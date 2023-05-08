package app.quranhub.ui.downloads_manager.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.Reciter
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.data.model.ReciterModel
import app.quranhub.data.repository.RecitationsRepository
import app.quranhub.databinding.DialogQuranRecitersBinding
import app.quranhub.ui.common.dialogs.OptionsListAdapter
import app.quranhub.ui.downloads_manager.dialogs.QuranRecitersDialogFragment.ReciterSelectionListener
import app.quranhub.util.DialogUtils.adjustDialogSize
import app.quranhub.util.FragmentUtils.isSafeFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableSingleObserver

/**
 * A `DialogFragment` that displays the available Quran reciters for the user to choose from.
 *
 *
 * Activities or parent fragments that shows this DialogFragment must implement the
 * [ReciterSelectionListener] interface to handle interaction events.
 * Use the [QuranRecitersDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuranRecitersDialogFragment : DialogFragment(), OptionsListAdapter.ItemClickListener {

    private var recitationId = 0
    private var selectedReciterId: String? = null
    private var selectedReciterIndex = -1
    private var binding: DialogQuranRecitersBinding? = null
    private var adapter: OptionsListAdapter? = null
    private var reciterSelectionListener: ReciterSelectionListener? = null
    private val recitationsRepository = RecitationsRepository()
    private var reciterModels: List<ReciterModel>? = null
    private val compositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        reciterSelectionListener = if (context is ReciterSelectionListener) {
            context
        } else if (parentFragment is ReciterSelectionListener) {
            parentFragment as ReciterSelectionListener?
        } else {
            throw RuntimeException(
                "Activities or parent fragments that shows this DialogFragment"
                        + " must implement ReciterSelectionListener"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            recitationId = it.getInt(ARG_RECITATION_ID)
            selectedReciterId = it.getString(ARG_SELECTED_RECITER_ID)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogQuranRecitersBinding.inflate(inflater, container, false)
        initDialogView()
        return binding!!.root
    }

    private fun initDialogView() {
        binding!!.tvMsgDownloadedRecitersOnly.visibility = View.GONE
        binding!!.tvMsgInternetConnectionFailed.visibility = View.GONE
        binding!!.progressBar.visibility = View.VISIBLE
        binding!!.btnSelect.isEnabled = false
        attachListeners()
    }

    private fun attachListeners() {
        binding!!.btnSelect.setOnClickListener { onSelectClick() }
        binding!!.btnBack.setOnClickListener { onBackClick() }
    }

    private fun setupRecitersRecyclerView() {
        if (reciterModels != null) {
            val recitersNames: MutableList<String> = ArrayList()
            for (i in reciterModels!!.indices) {
                val r = reciterModels!![i]
                recitersNames.add(r.getLocalizedName(requireContext()))
                if (r.id == selectedReciterId) {
                    selectedReciterIndex = i
                    binding!!.btnSelect.isEnabled = true
                }
            }
            binding!!.rvReciters.setHasFixedSize(true)
            binding!!.rvReciters.layoutManager = LinearLayoutManager(
                context, RecyclerView.VERTICAL, false
            )
            binding!!.rvReciters.addItemDecoration(
                DividerItemDecoration(
                    context, DividerItemDecoration.VERTICAL
                )
            )
            adapter = OptionsListAdapter(recitersNames, selectedReciterIndex, this)
            binding!!.rvReciters.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        adjustDialogSize(this)
    }

    override fun onStart() {
        super.onStart()
        var recitationKey: String? = null
        if (recitationId == Constants.Recitation.HAFS_ID) recitationKey =
            Constants.Recitation.HAFS_KEY else if (recitationId == Constants.Recitation.WARSH_ID) recitationKey =
            Constants.Recitation.WARSH_KEY
        val disposable: Disposable = recitationsRepository.getRecitersForRecitation(recitationKey!!)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<List<ReciterModel>>() {

                override fun onSuccess(reciters: List<ReciterModel>) {
                    Log.d(TAG, "reciters: $reciters")
                    if (isSafeFragment(this@QuranRecitersDialogFragment)) {
                        if (reciters.isNotEmpty()) {
                            reciterModels = reciters
                            setupRecitersRecyclerView()
                        } else {
                            Log.e(TAG, "The fetched reciters list is empty!")
                            Toast.makeText(
                                requireContext(), R.string.no_reciters,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        binding!!.progressBar.visibility = View.GONE
                    }
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "Error fetching reciters", e)
                    if (isSafeFragment(this@QuranRecitersDialogFragment)) {

                        // try to display the downloaded reciters
                        loadRecitersFromDb()
                    }
                }
            })
        compositeDisposable.add(disposable)
    }

    @SuppressLint("StaticFieldLeak")
    private fun loadRecitersFromDb() {
        // TODO load from DB only for a selected aya, or page or sura, to guarantee reciter is downloaded
        object : AsyncTask<Void?, Void?, List<ReciterModel>?>() {

            override fun doInBackground(vararg voids: Void?): List<ReciterModel>? {
                if (context != null) {
                    val reciterDao = UserDatabase.getInstance(context!!).reciterDao
                    val recitersList = reciterDao.getAllForRecitation(recitationId)

                    // Convert from Reciter to ReciterModel
                    val reciterModelsList: MutableList<ReciterModel> = ArrayList(recitersList.size)
                    for (r in recitersList) {
                        reciterModelsList.add(
                            ReciterModel(
                                r.id,
                                r.name,
                                r.nationality,
                                r.audioBaseUrl
                            )
                        )
                    }
                    return reciterModelsList
                }
                return null
            }

            override fun onPostExecute(recitersList: List<ReciterModel>?) {
                if (recitersList != null &&
                    isSafeFragment(this@QuranRecitersDialogFragment)
                ) {
                    reciterModels = recitersList
                    if (reciterModels!!.isNotEmpty()) {
                        binding!!.tvMsgDownloadedRecitersOnly.visibility = View.VISIBLE
                        setupRecitersRecyclerView()
                    } else {
                        // User hasn't downloaded any Quran audio before
                        binding!!.tvMsgInternetConnectionFailed.visibility = View.VISIBLE
                    }
                    binding!!.progressBar.visibility = View.GONE
                }
            }
        }.execute()
    }

    override fun onItemClick(clickedItemIndex: Int) {
        selectedReciterIndex = clickedItemIndex
        if (!binding!!.btnSelect.isEnabled) binding!!.btnSelect.isEnabled = true
    }

    private fun onBackClick() {
        dismiss()
    }

    @SuppressLint("StaticFieldLeak")
    private fun onSelectClick() {
        val selectedReciterModel = reciterModels!![selectedReciterIndex]

        object : AsyncTask<Void?, Void?, Void?>() {
            override fun onPreExecute() {
                binding!!.btnSelect.isEnabled = false
            }

            override fun doInBackground(vararg voids: Void?): Void? {
                if (isSafeFragment(this@QuranRecitersDialogFragment)) {

                    // Store selected reciter in DB
                    val userDatabase = UserDatabase.getInstance(requireContext())
                    if (userDatabase.reciterDao.getById(selectedReciterModel.id) == null) {
                        userDatabase.reciterDao.insert(
                            Reciter(
                                selectedReciterModel.id,
                                selectedReciterModel.getLocalizedName(requireContext()),
                                selectedReciterModel.getLocalizedNationality(requireContext()),
                                selectedReciterModel.audioBaseUrl
                            )
                        )
                    }

                    // persist selected reciter as preference if recitation id matches the one in preferences
                    val recitationIdPreference =
                        AppPreferencesManager.getRecitationSetting(requireContext())
                    if (recitationIdPreference == recitationId) {
                        AppPreferencesManager.persistReciterSheikhSetting(
                            requireContext(),
                            selectedReciterModel.id
                        )
                    }
                }
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                reciterSelectionListener!!.onReciterSelected(recitationId, selectedReciterModel)
                dismiss()
            }
        }.execute()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDetach() {
        super.onDetach()
        reciterSelectionListener = null
    }

    /**
     * This interface must be implemented by activities or parent fragments that contain this
     * dialog fragment to allow an interaction in this fragment to be communicated
     * to the activity or parent fragment.
     */
    interface ReciterSelectionListener {
        fun onReciterSelected(recitationId: Int, reciterModel: ReciterModel)
    }

    companion object {

        private val TAG = QuranRecitersDialogFragment::class.java.simpleName

        private const val ARG_RECITATION_ID = "ARG_RECITATION_ID"
        private const val ARG_SELECTED_RECITER_ID = "ARG_SELECTED_RECITER_ID"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param recitationId      A recitation ID as in [Constants.Recitation].
         * @param selectedReciterId The current selected reciter ID.
         * @return A new instance of fragment QuranRecitersDialogFragment.
         */
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param recitationId A recitation ID as in [Constants.Recitation].
         * @return A new instance of fragment QuranRecitersDialogFragment.
         */
        @JvmStatic
        @JvmOverloads
        fun newInstance(
            recitationId: Int,
            selectedReciterId: String? = null
        ): QuranRecitersDialogFragment {
            val recitersDialogFragment = QuranRecitersDialogFragment()
            val args = Bundle()
            args.putInt(ARG_RECITATION_ID, recitationId)
            args.putString(ARG_SELECTED_RECITER_ID, selectedReciterId)
            recitersDialogFragment.arguments = args
            return recitersDialogFragment
        }
    }
}