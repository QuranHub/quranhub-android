package app.quranhub.ui.mushaf.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.quranhub.R
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager.getQuranTranslationLanguage
import app.quranhub.databinding.FragmentLibraryBinding
import app.quranhub.ui.main.MainActivity
import app.quranhub.ui.mushaf.fragments.TranslationsDataFragment.TranslationSelectionListener

// TODO completely refactor LibraryFragment
class LibraryFragment : Fragment(), TranslationSelectionListener {

    private var binding: FragmentLibraryBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            val translationsDataFragment = TranslationsDataFragment.newInstance(
                getQuranTranslationLanguage(requireContext())
            )
            childFragmentManager.beginTransaction()
                .add(R.id.container_data_fragment, translationsDataFragment, "TransDataFragment")
                .commit()
        }
    }

    fun search(input: String?) {
        val translationsDataFragment = childFragmentManager
            .findFragmentByTag("TransDataFragment") as TranslationsDataFragment?
        translationsDataFragment?.search(input)
    }

    override fun onTranslationSelected(translationBook: TranslationBook) {
        val activity = activity as MainActivity?
        activity?.openTafseerScreen(translationBook.databaseName, translationBook.name)
    }

    companion object {
        private val TAG = LibraryFragment::class.java.simpleName
    }
}