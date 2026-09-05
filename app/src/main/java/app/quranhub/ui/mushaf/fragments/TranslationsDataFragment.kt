package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.R
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager.getQuranTranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager.persistBookDbName
import app.quranhub.data.local.prefs.AppPreferencesManager.persistBookName
import app.quranhub.data.local.prefs.AppPreferencesManager.persistQuranTranslationBook
import app.quranhub.databinding.FragmentTranslationsDataBinding
import app.quranhub.ui.common.interfaces.Searchable
import app.quranhub.ui.mushaf.adapter.TranslationsAdapter
import app.quranhub.ui.mushaf.model.DisplayableTranslation
import app.quranhub.ui.mushaf.viewmodel.TranslationsViewModel
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [TranslationsDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TranslationsDataFragment : Fragment(), Searchable, TranslationsAdapter.ItemClickListener {

    private var languageCode: String? = null
    private var listener: TranslationSelectionListener? = null
    private var binding: FragmentTranslationsDataBinding? = null
    private var adapter: TranslationsAdapter? = null

    private val viewModel: TranslationsViewModel by viewModels {
        viewModelFactory {
            initializer {
                TranslationsViewModel(requireActivity().application, languageCode)
            }
        }
    }

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
        adapter = TranslationsAdapter(
            ArrayList<DisplayableTranslation>(),
            getQuranTranslationBook(requireContext()),
            this
        )
        binding!!.rvTranslations.adapter = adapter
        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        binding!!.progressTranslation.visibility =
                            if (state.loading) View.VISIBLE else View.GONE
                        adapter!!.setTranslations(state.translations.toMutableList())
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is TranslationsViewModel.TranslationsEvent.FetchFailed -> {
                                Toast.makeText(
                                    context,
                                    getString(R.string.error_translations_web_service),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is TranslationsViewModel.TranslationsEvent.DownloadFailed -> {
                                Toast.makeText(
                                    context,
                                    getString(R.string.error_download_translation),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
        viewModel.downloadTranslation(translationBook!!)
    }

    override fun onCancelDownloadTranslationClick(
        translationBook: TranslationBook?,
        clickedItemIndex: Int
    ) {
        viewModel.cancelDownload(translationBook!!)
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
