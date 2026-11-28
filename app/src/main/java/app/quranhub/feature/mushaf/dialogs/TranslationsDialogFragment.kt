package app.quranhub.feature.mushaf.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import app.quranhub.R
import app.quranhub.core.common.util.DialogUtils.adjustDialogSize
import app.quranhub.core.data.local.entity.TranslationBook
import app.quranhub.feature.mushaf.fragments.TranslationsDataFragment
import app.quranhub.feature.mushaf.fragments.TranslationsDataFragment.TranslationSelectionListener

/**
 * A dialog that displays translation books for a language & allows the user to download & select one.
 *
 * Results are delivered via the Fragment Result API
 * ([androidx.fragment.app.FragmentManager.setFragmentResult]) using the [requestKey] supplied to
 * [newInstance]. Register a listener on the same FragmentManager the dialog is shown with:
 *
 * ```
 * childFragmentManager.setFragmentResultListener(
 *     TranslationsDialogFragment.REQUEST_KEY, viewLifecycleOwner
 * ) { _, bundle ->
 *     onTranslationSelected(TranslationsDialogFragment.bookFromResult(bundle))
 * }
 * ```
 *
 * The legacy setTargetFragment mechanism is intentionally not used: it throws
 * IllegalStateException ("declared target fragment ... does not belong to this
 * FragmentManager") when the dialog outlives its target (replace/navigation,
 * process-death restore). See Crashlytics f7817e39e76b1c0284cfbeee6eb4548a
 * (tag=trans_dialog). PR #295 migrated OptionsListDialogFragment; this dialog
 * was left on the legacy path.
 */
class TranslationsDialogFragment : DialogFragment(), TranslationSelectionListener {

    private var languageCode: String? = null
    private var requestKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            languageCode = it.getString(ARG_LANGUAGE_CODE)
            requestKey = it.getString(ARG_REQUEST_KEY)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    override fun onTranslationSelected(translationBook: TranslationBook) {
        requestKey?.let { key ->
            parentFragmentManager.setFragmentResult(
                key,
                Bundle().apply { putBook(this, translationBook) }
            )
        }
        dismiss()
    }

    companion object {

        const val REQUEST_KEY = "TranslationsDialogFragment:translation_selected"

        const val RESULT_BOOK_ID = "RESULT_BOOK_ID"
        const val RESULT_BOOK_NAME = "RESULT_BOOK_NAME"
        const val RESULT_BOOK_AUTHOR = "RESULT_BOOK_AUTHOR"
        const val RESULT_BOOK_LANGUAGE = "RESULT_BOOK_LANGUAGE"
        const val RESULT_BOOK_FILE_DOWNLOAD_PATH = "RESULT_BOOK_FILE_DOWNLOAD_PATH"
        const val RESULT_BOOK_DATABASE_NAME = "RESULT_BOOK_DATABASE_NAME"
        const val RESULT_BOOK_DOWNLOAD_STATUS = "RESULT_BOOK_DOWNLOAD_STATUS"
        const val RESULT_BOOK_DOWNLOAD_LEVEL_PERCENTAGE = "RESULT_BOOK_DOWNLOAD_LEVEL_PERCENTAGE"

        private const val ARG_LANGUAGE_CODE = "ARG_LANGUAGE_CODE"
        private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"

        /**
         * Reconstruct the selected [TranslationBook] from a Fragment Result bundle.
         */
        @JvmStatic
        fun bookFromResult(bundle: Bundle): TranslationBook {
            return TranslationBook(
                id = requireNotNull(bundle.getString(RESULT_BOOK_ID)),
                name = requireNotNull(bundle.getString(RESULT_BOOK_NAME)),
                author = requireNotNull(bundle.getString(RESULT_BOOK_AUTHOR)),
                language = requireNotNull(bundle.getString(RESULT_BOOK_LANGUAGE)),
                fileDownloadPath = requireNotNull(bundle.getString(RESULT_BOOK_FILE_DOWNLOAD_PATH)),
                databaseName = requireNotNull(bundle.getString(RESULT_BOOK_DATABASE_NAME)),
                downloadStatus = bundle.getInt(RESULT_BOOK_DOWNLOAD_STATUS),
                downloadLevelPercentage = bundle.getInt(RESULT_BOOK_DOWNLOAD_LEVEL_PERCENTAGE)
            )
        }

        /**
         * Use this factory method to create a new instance of
         * this dialog fragment using the provided parameters.
         *
         * @param languageCode translation language to list books for
         * @param requestKey Fragment Result API key; hosts must register a listener
         * for the same key on the FragmentManager the dialog is shown with
         * @return A new instance of fragment TranslationsDialogFragment.
         */
        @JvmStatic
        fun newInstance(
            languageCode: String?,
            requestKey: String = REQUEST_KEY
        ): TranslationsDialogFragment {
            val fragment = TranslationsDialogFragment()
            val args = Bundle()
            args.putString(ARG_LANGUAGE_CODE, languageCode)
            args.putString(ARG_REQUEST_KEY, requestKey)
            fragment.arguments = args
            return fragment
        }

        private fun putBook(bundle: Bundle, book: TranslationBook) {
            bundle.putString(RESULT_BOOK_ID, book.id)
            bundle.putString(RESULT_BOOK_NAME, book.name)
            bundle.putString(RESULT_BOOK_AUTHOR, book.author)
            bundle.putString(RESULT_BOOK_LANGUAGE, book.language)
            bundle.putString(RESULT_BOOK_FILE_DOWNLOAD_PATH, book.fileDownloadPath)
            bundle.putString(RESULT_BOOK_DATABASE_NAME, book.databaseName)
            bundle.putInt(RESULT_BOOK_DOWNLOAD_STATUS, book.downloadStatus)
            bundle.putInt(RESULT_BOOK_DOWNLOAD_LEVEL_PERCENTAGE, book.downloadLevelPercentage)
        }
    }
}
