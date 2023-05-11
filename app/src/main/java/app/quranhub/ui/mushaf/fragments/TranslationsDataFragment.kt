package app.quranhub.ui.mushaf.fragments

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.R
import app.quranhub.data.local.db.UserDatabase.Companion.getInstance
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager.getQuranTranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager.persistBookDbName
import app.quranhub.data.local.prefs.AppPreferencesManager.persistBookName
import app.quranhub.data.local.prefs.AppPreferencesManager.persistQuranTranslationBook
import app.quranhub.data.remote.ApiClient.client
import app.quranhub.data.remote.TranslationDownloader
import app.quranhub.data.remote.TranslationDownloader.TranslationDownloadCallback
import app.quranhub.data.remote.api.TranslationsApi
import app.quranhub.data.remote.model.TranslationsResponse
import app.quranhub.databinding.FragmentTranslationsDataBinding
import app.quranhub.ui.common.interfaces.Searchable
import app.quranhub.ui.mushaf.adapter.TranslationsAdapter
import app.quranhub.ui.mushaf.model.DisplayableTranslation
import app.quranhub.util.FragmentUtils.isSafeFragment
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 * Use the [TranslationsDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
// TODO apply MVVM
class TranslationsDataFragment : Fragment(), Searchable, TranslationsAdapter.ItemClickListener,
    PermissionCallbacks, TranslationDownloadCallback {

    private val searchText = ""
    private var languageCode: String? = null
    private var listener: TranslationSelectionListener? = null
    private var binding: FragmentTranslationsDataBinding? = null
    private var displayableTranslations: MutableList<DisplayableTranslation>? = null
    private var adapter: TranslationsAdapter? = null
    var remoteTranslationBooks: List<TranslationBook>? = null
    var translationBooksLiveData: LiveData<List<TranslationBook?>?>? = null
    var translationDownloaders: MutableList<TranslationDownloader>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (parentFragment is TranslationSelectionListener) {
            parentFragment as TranslationSelectionListener?
        } else {
            error(
                "${requireParentFragment().javaClass.simpleName} must implement TranslationsDataFragment#TranslationSelectionListener"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            languageCode = requireArguments().getString(ARG_LANGUAGE_CODE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTranslationsDataBinding.inflate(inflater, container, false)
        initView()
        return binding!!.root
    }

    private fun initView() {
        // setup translationsRecyclerView
        binding!!.rvTranslations.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding!!.rvTranslations.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            layoutManager.orientation
        )
        binding!!.rvTranslations.addItemDecoration(dividerItemDecoration)
        displayableTranslations = ArrayList()
        adapter = TranslationsAdapter(
            displayableTranslations,
            getQuranTranslationBook(requireContext()),
            this
        )
        binding!!.rvTranslations.adapter = adapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        translationBooksLiveData = getInstance(requireContext())
            .translationBookDao
            .getByLanguage(languageCode)
        setupTranslationBooksLiveDataObserver()
        fetchTranslationBooks()
        translationDownloaders = ArrayList()
        if (savedInstanceState != null) {
            search(savedInstanceState.getString(STATE_SEARCH_TEXT))
        }
    }

    private fun fetchTranslationBooks() {
        val translationsApi = client!!.create(
            TranslationsApi::class.java
        )
        val translationsCall = translationsApi.allTranslations
        translationsCall!!.enqueue(object : Callback<TranslationsResponse?> {
            override fun onResponse(
                call: Call<TranslationsResponse?>,
                response: Response<TranslationsResponse?>
            ) {
                if (isSafeFragment(this@TranslationsDataFragment)) {
                    val translationsResponse = response.body()
                    if (translationsResponse != null) {
                        remoteTranslationBooks =
                            translationsResponse.getTranslationBooksForLanguage(languageCode)
                        if (remoteTranslationBooks != null) {
                            for (book in remoteTranslationBooks!!) {
                                val d = DisplayableTranslation(book)
                                if (!displayableTranslations!!.contains(d)) {
                                    // only add if it's not there
                                    displayableTranslations!!.add(d)
                                }
                            }
                            binding!!.progressTranslation.visibility = View.GONE
                            adapter!!.setTranslations(displayableTranslations)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<TranslationsResponse?>, t: Throwable) {
                Log.d(TAG, "onFailure - cause:  " + t.message)
                if (isSafeFragment(this@TranslationsDataFragment)) {
                    //progressBar.setVisibility(View.GONE);
                    Toast.makeText(
                        context,
                        getString(R.string.error_translations_web_service),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun setupTranslationBooksLiveDataObserver() {
        translationBooksLiveData!!.observe(viewLifecycleOwner) { localTranslationBooks: List<TranslationBook?>? ->
            Log.d(TAG, "translationBooksLiveData: localTranslationBooks = $localTranslationBooks")
            if (displayableTranslations!!.size > 0) {
                // there's a change in localTranslationBooks
                // merge objects in remoteTranslationBooks & localTranslationBooks
                displayableTranslations!!.clear()
                for (book in localTranslationBooks!!) {
                    displayableTranslations!!.add(DisplayableTranslation(book!!))
                }
                if (remoteTranslationBooks != null) {
                    for (book in remoteTranslationBooks!!) {
                        val d = DisplayableTranslation(book)
                        if (!displayableTranslations!!.contains(d)) {
                            // only add if it's not there
                            displayableTranslations!!.add(d)
                        }
                    }
                }
            } else {
                // displayableTranslations is empty
                // copy objects from localTranslationBooks
                for (book in localTranslationBooks!!) {
                    displayableTranslations!!.add(DisplayableTranslation(book!!))
                }
            }
            Log.d(
                TAG,
                "translationBooksLiveData: displayableTranslations = $displayableTranslations"
            )
            adapter!!.setTranslations(displayableTranslations)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_SEARCH_TEXT, searchText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        //Download the file once permission is granted
        translationDownloaders!![translationDownloaders!!.size - 1].download()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Log.d(TAG, "Permission has been denied")
    }

    override fun onTranslationClick(translationBook: TranslationBook?, clickedItemIndex: Int) {
        Log.d(TAG, "Clicked translation book: $translationBook")
        persistQuranTranslationBook(requireContext(), translationBook!!.id)
        persistBookDbName(requireActivity(), translationBook.databaseName)
        persistBookName(requireActivity(), translationBook.name)
        listener!!.onTranslationSelected(translationBook)
    }

    override fun onDownloadTranslationClick(
        translationBook: TranslationBook?,
        clickedItemIndex: Int
    ) {
        Log.d(TAG, "onDownloadTranslationClick: translationBook = $translationBook")
        val downloader = TranslationDownloader(translationBook!!, requireContext(), this)
        translationDownloaders!!.add(downloader)

        // check if app has permission to write to the external storage.
        if (EasyPermissions.hasPermissions(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            downloader.download()
        } else {
            // if permission is not present request for the same.
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.translation_dowload_permission_rationale),
                WRITE_REQUEST_CODE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onCancelDownloadTranslationClick(
        translationBook: TranslationBook?,
        clickedItemIndex: Int
    ) {
        Log.d(TAG, "onCancelDownloadTranslationClick: translationBook = $translationBook")
        for (downloader in translationDownloaders!!) {
            if (downloader.translationBook.id == translationBook!!.id) {
                Log.d(TAG, "Download canceled for : " + translationBook.id)
                downloader.cancel()
                translationDownloaders!!.remove(downloader)
                break
            }
        }
    }

    override fun onDownloadStarted() {}
    override fun onDownloadFinished() {}
    override fun onDownloadCancelled() {}

    override fun onDownloadFailed() {
        if (isSafeFragment(this)) {
            Toast.makeText(
                context,
                getString(R.string.error_download_translation),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun search(text: String?) {
        adapter!!.filter.filter(text)
    }

    /**
     * The target fragment must implement this interface.
     */
    interface TranslationSelectionListener {
        fun onTranslationSelected(translationBook: TranslationBook)
    }

    companion object {
        private val TAG = TranslationsDataFragment::class.java.simpleName

        private const val ARG_LANGUAGE_CODE = "ARG_LANGUAGE_CODE"
        private const val WRITE_REQUEST_CODE = 0
        private const val STATE_SEARCH_TEXT = "STATE_SEARCH_TEXT"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param languageCode
         * @return A new instance of fragment TranslationsDataFragment.
         */
        fun newInstance(languageCode: String?): TranslationsDataFragment {
            val fragment = TranslationsDataFragment()
            val args = Bundle()
            args.putString(ARG_LANGUAGE_CODE, languageCode)
            fragment.arguments = args
            return fragment
        }
    }
}