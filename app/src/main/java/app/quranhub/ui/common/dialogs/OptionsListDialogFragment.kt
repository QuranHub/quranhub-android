package app.quranhub.ui.common.dialogs

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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.ui.first_wizard.OptionsListAdapter
import app.quranhub.databinding.DialogOptionsListBinding
import app.quranhub.util.DialogUtils.adjustDialogSize

/**
 * Display options as a list (single selection)
 *
 * Results are delivered via the Fragment Result API ([androidx.fragment.app.FragmentManager.setFragmentResult])
 * using the [requestKey] supplied to [getInstance]. Register a listener on the same
 * FragmentManager the dialog is shown with:
 *
 * ```
 * childFragmentManager.setFragmentResultListener(REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
 *     onItemSelected(
 *         bundle.getInt(OptionsListDialogFragment.RESULT_REQUEST_CODE),
 *         bundle.getInt(OptionsListDialogFragment.RESULT_ITEM_INDEX)
 *     )
 * }
 * ```
 *
 * The legacy setTargetFragment mechanism is intentionally not used: it throws
 * IllegalStateException ("declared target fragment ... does not belong to this
 * FragmentManager") when the dialog outlives its target (replace/navigation,
 * process-death restore). See Crashlytics f7817e39e76b1c0284cfbeee6eb4548a.
 */
class OptionsListDialogFragment : DialogFragment(), OptionsListAdapter.ItemClickListener {

    private var dialogTitle: String? = null
    private var options: List<String>? = null
    private var optionsThumbnailsDrawableIds: IntArray? = null
    private var selectedOptionIndex = 0
    private var requestKey: String? = null
    private var requestCode = 0
    private var _binding: DialogOptionsListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialogTitle = it.getString(ARG_DIALOG_TITLE)
            options = it.getStringArrayList(ARG_DIALOG_OPTIONS)
            optionsThumbnailsDrawableIds = it.getIntArray(ARG_DIALOG_OPTIONS_THUMBNAILS)
            selectedOptionIndex = it.getInt(ARG_SELECTED_OPTION_INDEX)
            requestKey = it.getString(ARG_REQUEST_KEY)
            requestCode = it.getInt(ARG_REQUEST_CODE)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogOptionsListBinding.inflate(inflater, container, false)
        initDialogView()
        return binding.root
    }

    private fun initDialogView() {
        binding.tvTitle.text = dialogTitle
        binding.rvOptions.setHasFixedSize(true)
        binding.rvOptions.layoutManager = LinearLayoutManager(
            context, RecyclerView.VERTICAL, false
        )
        binding.rvOptions.addItemDecoration(
            DividerItemDecoration(
                context, DividerItemDecoration.VERTICAL
            )
        )
        val adapter = OptionsListAdapter(
            options!!, optionsThumbnailsDrawableIds, selectedOptionIndex, this
        )
        binding.rvOptions.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        adjustDialogSize(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(clickedItemIndex: Int) {
        requestKey?.let { key ->
            parentFragmentManager.setFragmentResult(
                key,
                Bundle().apply {
                    putInt(RESULT_REQUEST_CODE, requestCode)
                    putInt(RESULT_ITEM_INDEX, clickedItemIndex)
                }
            )
        }
        dismiss()
    }

    interface ItemSelectionListener {
        fun onItemSelected(requestCode: Int, itemIndex: Int)
    }

    companion object {
        const val RESULT_REQUEST_CODE = "RESULT_REQUEST_CODE"
        const val RESULT_ITEM_INDEX = "RESULT_ITEM_INDEX"

        private const val ARG_DIALOG_TITLE = "ARG_DIALOG_TITLE"
        private const val ARG_DIALOG_OPTIONS = "ARG_DIALOG_OPTIONS"
        private const val ARG_DIALOG_OPTIONS_THUMBNAILS = "ARG_DIALOG_OPTIONS_THUMBNAILS"
        private const val ARG_SELECTED_OPTION_INDEX = "ARG_SELECTED_OPTION_INDEX"
        private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"
        private const val ARG_REQUEST_CODE = "ARG_REQUEST_CODE"

        @JvmStatic
        fun getInstance(
            dialogTitle: String,
            options: List<String?>,
            requestKey: String,
            requestCode: Int
        ): OptionsListDialogFragment {
            return getInstance(dialogTitle, options, -1, requestKey, requestCode)
        }

        @JvmStatic
        fun getInstance(
            dialogTitle: String,
            optionsResIds: IntArray,
            context: Context,
            requestKey: String,
            requestCode: Int
        ): OptionsListDialogFragment {
            return getInstance(dialogTitle, optionsResIds, -1, context, requestKey, requestCode)
        }

        @JvmStatic
        fun getInstance(
            dialogTitle: String,
            optionsResIds: IntArray,
            selectedOptionIndex: Int,
            context: Context,
            requestKey: String,
            requestCode: Int
        ): OptionsListDialogFragment {
            val options: MutableList<String?> = ArrayList()
            for (stringResId in optionsResIds) {
                options.add(context.getString(stringResId))
            }
            return getInstance(
                dialogTitle,
                options,
                selectedOptionIndex,
                requestKey,
                requestCode
            )
        }

        @JvmStatic
        fun getInstance(
            dialogTitle: String,
            options: List<String?>,
            selectedOptionIndex: Int,
            requestKey: String,
            requestCode: Int
        ): OptionsListDialogFragment {
            val fragment = OptionsListDialogFragment()
            val args = Bundle()
            args.putString(ARG_DIALOG_TITLE, dialogTitle)
            args.putStringArrayList(ARG_DIALOG_OPTIONS, ArrayList(options))
            args.putInt(ARG_SELECTED_OPTION_INDEX, selectedOptionIndex)
            args.putString(ARG_REQUEST_KEY, requestKey)
            args.putInt(ARG_REQUEST_CODE, requestCode)
            fragment.arguments = args
            return fragment
        }

        @JvmStatic
        fun getInstance(
            dialogTitle: String,
            optionsResIds: IntArray,
            optionsThumbnailsDrawableIds: IntArray?,
            selectedOptionIndex: Int,
            context: Context,
            requestKey: String,
            requestCode: Int
        ): OptionsListDialogFragment {
            val options: MutableList<String> = ArrayList()
            for (stringResId in optionsResIds) {
                options.add(context.getString(stringResId))
            }
            val fragment = OptionsListDialogFragment()
            val args = Bundle()
            args.putString(ARG_DIALOG_TITLE, dialogTitle)
            args.putStringArrayList(ARG_DIALOG_OPTIONS, ArrayList(options))
            args.putIntArray(ARG_DIALOG_OPTIONS_THUMBNAILS, optionsThumbnailsDrawableIds)
            args.putInt(ARG_SELECTED_OPTION_INDEX, selectedOptionIndex)
            args.putString(ARG_REQUEST_KEY, requestKey)
            args.putInt(ARG_REQUEST_CODE, requestCode)
            fragment.arguments = args
            return fragment
        }
    }
}