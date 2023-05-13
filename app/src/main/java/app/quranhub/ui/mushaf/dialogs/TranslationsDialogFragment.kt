package app.quranhub.ui.mushaf.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import app.quranhub.R
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.ui.mushaf.fragments.TranslationsDataFragment
import app.quranhub.ui.mushaf.fragments.TranslationsDataFragment.TranslationSelectionListener
import app.quranhub.util.DialogUtils.adjustDialogSize

/**
 * A dialog that displays translation books for a language & allows the user to download & select one.
 * The target fragment must implement the interface `TranslationsDialogFragment#TranslationSelectionListener`
 */
class TranslationsDialogFragment : DialogFragment(), TranslationSelectionListener {

    private var languageCode: String? = null
    private var listener: TranslationSelectionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = if (targetFragment is TranslationSelectionListener) {
            targetFragment as TranslationSelectionListener?
        } else {
            error(
                "${targetFragment!!.javaClass.simpleName} must implement TranslationsDialogFragment#TranslationSelectionListener"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            languageCode = it.getString(ARG_LANGUAGE_CODE)
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
    ): View? {
        return inflater.inflate(R.layout.dialog_translations, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) {
            val translationsDataFragment = TranslationsDataFragment.newInstance(
                languageCode
            )
            childFragmentManager.beginTransaction()
                .add(R.id.container_translations_data, translationsDataFragment)
                .commit()
        }
    }

    override fun onResume() {
        super.onResume()
        adjustDialogSize(this)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onTranslationSelected(translationBook: TranslationBook) {
        dismiss()
        listener!!.onTranslationSelected(translationBook)
    }

    companion object {

        private val TAG = TranslationsDialogFragment::class.java.simpleName

        private const val ARG_LANGUAGE_CODE = "ARG_LANGUAGE_CODE"

        /**
         * Use this factory method to create a new instance of
         * this dialog fragment using the provided parameters.
         *
         * @param languageCode
         * @param targetFragment
         * @return A new instance of fragment TranslationsDialogFragment.
         */
        @JvmStatic
        fun newInstance(
            languageCode: String?, targetFragment: Fragment
        ): TranslationsDialogFragment {
            val fragment = TranslationsDialogFragment()
            val args = Bundle()
            args.putString(ARG_LANGUAGE_CODE, languageCode)
            fragment.arguments = args
            fragment.setTargetFragment(targetFragment, 0)
            return fragment
        }
    }
}