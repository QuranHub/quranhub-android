package app.quranhub.ui.mushaf.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.quranhub.R
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager.getQuranTranslationLanguage
import app.quranhub.databinding.FragmentTranslationsLibraryBinding
import app.quranhub.ui.common.interfaces.ToolbarActionsListener
import app.quranhub.ui.main.MainActivity
import app.quranhub.ui.mushaf.fragments.TranslationsDataFragment.TranslationSelectionListener

class TranslationsLibraryFragment : Fragment(), TranslationSelectionListener {

    private var translationsDataFragment: TranslationsDataFragment? = null
    private var inputSearch: String? = ""
    private var navDrawerListener: ToolbarActionsListener? = null

    private var binding: FragmentTranslationsLibraryBinding? = null

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
        binding = FragmentTranslationsLibraryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        restoreSavedInstanceState(savedInstanceState)
        addFragment()
        attachListeners()
    }

    private fun attachListeners() {
        observeOnInputSearch()
        binding!!.hamburgerIv.setOnClickListener { v: View? -> onNavHamburgerClick() }
    }

    private fun restoreSavedInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            inputSearch = savedInstanceState.getString(STATE_INPUT_SEARCH)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_INPUT_SEARCH, inputSearch)
    }

    private fun observeOnInputSearch() {
        binding!!.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                inputSearch = s.toString()
                translationsDataFragment?.search(inputSearch)
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun addFragment() {
        binding!!.etSearch.text.clear()
        inputSearch = ""

        translationsDataFragment =
            childFragmentManager.findFragmentByTag(FRAGMENT_TRANSLATION_DATA) as TranslationsDataFragment?
        if (translationsDataFragment == null) {
            translationsDataFragment = TranslationsDataFragment.newInstance(
                getQuranTranslationLanguage(requireContext())
            )
            childFragmentManager.beginTransaction()
                .replace(R.id.data_container, translationsDataFragment!!, FRAGMENT_TRANSLATION_DATA)
                .commit()
        }
    }

    private fun onNavHamburgerClick() {
        navDrawerListener!!.onNavDrawerClick()
    }

    override fun onTranslationSelected(translationBook: TranslationBook) {
        val activity = activity as MainActivity?
        activity?.openTafseerScreen(translationBook.databaseName, translationBook.name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val FRAGMENT_TRANSLATION_DATA = "FRAGMENT_TRANSLATION_DATA"
        private const val STATE_INPUT_SEARCH = "STATE_INPUT_SEARCH"
    }
}