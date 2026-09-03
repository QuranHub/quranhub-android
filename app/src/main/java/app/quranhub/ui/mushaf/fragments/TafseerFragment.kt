package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager.getQuranTranslationLanguage
import app.quranhub.data.local.prefs.AppPreferencesManager.persistQuranTranslationLanguage
import app.quranhub.databinding.FragmentTafseerBinding
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment
import app.quranhub.ui.common.dialogs.OptionsListDialogFragment.Companion.getInstance
import app.quranhub.ui.common.interfaces.ToolbarActionsListener
import app.quranhub.ui.mushaf.adapter.TafseerAdapter
import app.quranhub.ui.mushaf.dialogs.OptionDialog
import app.quranhub.ui.mushaf.dialogs.OptionDialog.Companion.getInstance
import app.quranhub.ui.mushaf.dialogs.TranslationsDialogFragment
import app.quranhub.ui.mushaf.fragments.TranslationsDataFragment.TranslationSelectionListener
import app.quranhub.ui.mushaf.model.TafseerModel
import app.quranhub.ui.mushaf.viewmodel.TafseerViewModel
import app.quranhub.util.InsetsUtils
import kotlinx.coroutines.launch

class TafseerFragment : Fragment(), OptionDialog.ItemClickListener, TranslationSelectionListener,
    OptionsListDialogFragment.ItemSelectionListener {

    private var binding: FragmentTafseerBinding? = null

    private var inputSearch: String? = ""
    private var navDrawerListener: ToolbarActionsListener? = null
    private var suraName: String? = null
    private var bookDbName: String? = null
    private var bookName: String? = null
    private var suraNumber = 0
    private var ayaNumber = 0
    private var adapter: TafseerAdapter? = null
    private var viewModel: TafseerViewModel? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ToolbarActionsListener) {
            navDrawerListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTafseerBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        InsetsUtils.padTopForStatusBar(binding!!.toolbarLayout)
        readArgumentsData()
        savedInstanceState?.let { getPrevState(it) }
        initRecycler()
        bindViewModel()
        attachListeners()
    }

    private fun attachListeners() {
        observeOnInputSearch()
        binding!!.filterSuraBtn.setOnClickListener { onOpenSuraFilter() }
        binding!!.filterBookBtn.setOnClickListener { onOpenBooksFilter() }
        binding!!.filterLangBtn.setOnClickListener { onOpenLangFilter() }
        binding!!.hamburgerIv.setOnClickListener { onNavHamburgerClick() }
    }

    private fun getPrevState(savedInstanceState: Bundle) {
        inputSearch = savedInstanceState.getString("input_search")
        suraName = savedInstanceState.getString("sura_name")
        suraNumber = savedInstanceState.getInt("sura_number")
        binding!!.suraTv.text = suraName
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("input_search", inputSearch)
        outState.putString("sura_name", suraName)
        outState.putInt("sura_number", suraNumber)
    }

    private fun initRecycler() {
        adapter = TafseerAdapter(requireActivity())
        binding!!.tafseerRv.layoutManager = LinearLayoutManager(activity)
        binding!!.tafseerRv.setHasFixedSize(true)
        binding!!.tafseerRv.adapter = adapter
    }

    private fun bindViewModel() {
        viewModel = ViewModelProvider(this)[TafseerViewModel::class.java]
        viewModel!!.setSelectedBook(bookDbName, bookName)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel!!.uiState.collect { uiState ->
                        renderUiState(uiState)
                    }
                }
                launch {
                    viewModel!!.tafseerEvents.collect { event ->
                        renderEvent(event)
                    }
                }
            }
        }
        viewModel!!.loadInitialTafseers(suraNumber)
    }

    private fun renderUiState(uiState: TafseerViewModel.TafseerUiState) {
        binding!!.progreesBar.visibility = if (uiState.loading) View.VISIBLE else View.GONE
        uiState.bookName?.let { bookName -> binding!!.bookTv.text = bookName }
        if (uiState.loading) {
            return
        }
        val tafseerModels = uiState.tafseerItems
        adapter!!.setTafseerModelList(tafseerModels)
        if (ayaNumber <= tafseerModels.size) {
            binding!!.tafseerRv.scrollToPosition(ayaNumber - 1)
        } else {
            binding!!.tafseerRv.scrollToPosition(0)
        }
        if (inputSearch != null && !TextUtils.isEmpty(inputSearch!!.trim { it <= ' ' })) {
            adapter!!.filter(inputSearch!!)
        }
    }

    private fun renderEvent(event: TafseerViewModel.TafseerEvent) {
        when (event) {
            is TafseerViewModel.TafseerEvent.NoDownloadedBooks -> {
                binding!!.progreesBar.visibility = View.GONE
                binding!!.bookTv.text = getString(R.string.choose_book)
                Toast.makeText(activity, getString(R.string.no_downloaded_books), Toast.LENGTH_LONG)
                    .show()
            }
            is TafseerViewModel.TafseerEvent.DataLoadFailed -> {
                Toast.makeText(activity, getString(R.string.data_failed), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeOnInputSearch() {
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                adapter!!.filter(s.toString())
                inputSearch = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun readArgumentsData() {
        if (arguments != null) {
            suraName = requireArguments().getString(ARG_SURA_NAME)
            suraNumber = requireArguments().getInt(ARG_SURA_NUMBER)
            bookDbName = requireArguments().getString(ARG_BOOK_DB_NAME)
            bookName = requireArguments().getString(ARG_BOOK_NAME)
            ayaNumber = requireArguments().getInt("ARG_AYA_NUMBER")
            binding!!.suraTv.text = suraName
        }
        val currentTranslationLanguageIndex = Constants.Language.CODES.indexOf(
            getQuranTranslationLanguage(requireContext())
        )
        binding!!.langTv.text = getString(
            Constants.Language.NAMES_STR_IDS[currentTranslationLanguageIndex]
        )
    }

    private fun onOpenSuraFilter() {
        val optionsArr = resources.getStringArray(R.array.sura_name)
        val options = ArrayList<String?>()
        options.addAll(listOf(*optionsArr))
        val fragment = getInstance(
            options, suraName, 1, getString(R.string.suras)
        )
        fragment.show(childFragmentManager, "trans_sura_dialog")
    }

    private fun onOpenBooksFilter() {
        val transLang = getQuranTranslationLanguage(requireContext())
        val translationsDialog = TranslationsDialogFragment.newInstance(
            transLang, this
        )
        translationsDialog.show(parentFragmentManager, "trans_book_dialog")
    }

    private fun onOpenLangFilter() {
        val currentTranslationLanguageIndex = Constants.Language.CODES.indexOf(
            getQuranTranslationLanguage(requireContext())
        )
        val translationLangDialog = getInstance(
            getString(R.string.translation_lang_setting_dialog_title),
            Constants.Language.NAMES_STR_IDS,
            currentTranslationLanguageIndex,
            this,
            RC_TRANS_LANG_SETTING
        )
        translationLangDialog.show(parentFragmentManager, "trans_lang_dialog")
    }

    private fun onNavHamburgerClick() {
        navDrawerListener!!.onNavDrawerClick()
    }

    override fun onTranslationSelected(translationBook: TranslationBook) {
        viewModel!!.onBookSelected(
            translationBook.databaseName, translationBook.id, translationBook.name
        )
    }

    override fun onItemSelected(requestCode: Int, itemIndex: Int) {
        val langCode = Constants.Language.CODES[itemIndex]
        persistQuranTranslationLanguage(requireContext(), langCode)
        viewModel!!.onTranslationLanguageChanged(langCode)
        binding!!.langTv.text =
            getString(Constants.Language.NAMES_STR_IDS[itemIndex])
    }

    override fun onItemClick(optionName: String?, optionIndex: Int, requestCode: Int) {
        binding!!.etSearch.text.clear()
        suraName = optionName
        suraNumber = optionIndex + 1
        ayaNumber = 1
        viewModel!!.onSuraSelected(suraNumber)
        binding!!.suraTv.text = suraName
    }

    companion object {

        private const val RC_TRANS_LANG_SETTING = 2
        private const val ARG_SURA_NAME = "ARG_SURA_NAME"
        private const val ARG_SURA_NUMBER = "ARG_PAGE_NUMBER"
        private const val ARG_BOOK_DB_NAME = "ARG_BOOK_DB_NAME"
        private const val ARG_BOOK_NAME = "ARG_BOOK_NAME"
        private const val ARG_AYA_NUMBER = "ARG_AYA_NUMBER"

        fun newInstance(
            suraName: String?,
            suraNumber: Int,
            bookDbName: String?,
            bookName: String?,
            ayaNumber: Int
        ): TafseerFragment {
            val tafseerFragment = TafseerFragment()
            val bundle = Bundle()
            bundle.putString(ARG_SURA_NAME, suraName)
            bundle.putInt(ARG_SURA_NUMBER, suraNumber)
            bundle.putString(ARG_BOOK_DB_NAME, bookDbName)
            bundle.putString(ARG_BOOK_NAME, bookName)
            bundle.putInt(ARG_AYA_NUMBER, ayaNumber)
            tafseerFragment.arguments = bundle
            return tafseerFragment
        }
    }
}
