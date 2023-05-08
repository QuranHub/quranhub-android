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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.quranhub.databinding.DialogOptionsListBinding
import app.quranhub.util.DialogUtils.adjustDialogSize

/**
 * Display options as a list (single selection)
 */
class OptionsListDialogFragment : DialogFragment(), OptionsListAdapter.ItemClickListener {

    private var dialogTitle: String? = null
    private var options: List<String>? = null
    private var optionsThumbnailsDrawableIds: IntArray? = null
    private var selectedOptionIndex = 0
    private var binding: DialogOptionsListBinding? = null
    private var itemSelectionListener: ItemSelectionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        itemSelectionListener = try {
            targetFragment as ItemSelectionListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(
                targetFragment!!.javaClass.simpleName
                        + " must implement OptionsListDialogFragment#ItemSelectionListener"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dialogTitle = it.getString(ARG_DIALOG_TITLE)
            options = it.getStringArrayList(ARG_DIALOG_OPTIONS)
            optionsThumbnailsDrawableIds = it.getIntArray(ARG_DIALOG_OPTIONS_THUMBNAILS)
            selectedOptionIndex = it.getInt(ARG_SELECTED_OPTION_INDEX)
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
        binding = DialogOptionsListBinding.inflate(inflater, container, false)
        initDialogView()
        return binding!!.root
    }

    private fun initDialogView() {
        binding!!.tvTitle.text = dialogTitle
        binding!!.rvOptions.setHasFixedSize(true)
        binding!!.rvOptions.layoutManager = LinearLayoutManager(
            context, RecyclerView.VERTICAL, false
        )
        binding!!.rvOptions.addItemDecoration(
            DividerItemDecoration(
                context, DividerItemDecoration.VERTICAL
            )
        )
        val adapter = OptionsListAdapter(
            options!!, optionsThumbnailsDrawableIds, selectedOptionIndex, this
        )
        binding!!.rvOptions.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        adjustDialogSize(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDetach() {
        super.onDetach()
        itemSelectionListener = null
    }

    override fun onItemClick(clickedItemIndex: Int) {
        itemSelectionListener!!.onItemSelected(targetRequestCode, clickedItemIndex)
        dismiss()
    }

    interface ItemSelectionListener {
        fun onItemSelected(requestCode: Int, itemIndex: Int)
    }

    companion object {
        private val TAG = OptionsListDialogFragment::class.java.simpleName

        private const val ARG_DIALOG_TITLE = "ARG_DIALOG_TITLE"
        private const val ARG_DIALOG_OPTIONS = "ARG_DIALOG_OPTIONS"
        private const val ARG_DIALOG_OPTIONS_THUMBNAILS = "ARG_DIALOG_OPTIONS_THUMBNAILS"
        private const val ARG_SELECTED_OPTION_INDEX = "ARG_SELECTED_OPTION_INDEX"

        @JvmStatic
        fun getInstance(
            dialogTitle: String, options: List<String?>, targetFragment: Fragment, requestCode: Int
        ): OptionsListDialogFragment {
            return getInstance(dialogTitle, options, -1, targetFragment, requestCode)
        }

        @JvmStatic
        fun getInstance(
            dialogTitle: String, optionsResIds: IntArray, targetFragment: Fragment, requestCode: Int
        ): OptionsListDialogFragment {
            return getInstance(dialogTitle, optionsResIds, -1, targetFragment, requestCode)
        }

        @JvmStatic
        fun getInstance(
            dialogTitle: String,
            optionsResIds: IntArray,
            selectedOptionIndex: Int,
            targetFragment: Fragment,
            requestCode: Int
        ): OptionsListDialogFragment {
            val options: MutableList<String?> = ArrayList()
            for (stringResId in optionsResIds) {
                options.add(targetFragment.getString(stringResId))
            }
            return getInstance(
                dialogTitle,
                options,
                selectedOptionIndex,
                targetFragment,
                requestCode
            )
        }

        @JvmStatic
        fun getInstance(
            dialogTitle: String,
            options: List<String?>,
            selectedOptionIndex: Int,
            targetFragment: Fragment,
            requestCode: Int
        ): OptionsListDialogFragment {
            val fragment = OptionsListDialogFragment()
            val args = Bundle()
            args.putString(ARG_DIALOG_TITLE, dialogTitle)
            args.putStringArrayList(ARG_DIALOG_OPTIONS, ArrayList(options))
            args.putInt(ARG_SELECTED_OPTION_INDEX, selectedOptionIndex)
            fragment.arguments = args
            fragment.setTargetFragment(targetFragment, requestCode)
            return fragment
        }

        @JvmStatic
        fun getInstance(
            dialogTitle: String,
            optionsResIds: IntArray,
            optionsThumbnailsDrawableIds: IntArray?,
            selectedOptionIndex: Int,
            targetFragment: Fragment,
            requestCode: Int
        ): OptionsListDialogFragment {
            val options: MutableList<String> = ArrayList()
            for (stringResId in optionsResIds) {
                options.add(targetFragment.getString(stringResId))
            }
            val fragment = OptionsListDialogFragment()
            val args = Bundle()
            args.putString(ARG_DIALOG_TITLE, dialogTitle)
            args.putStringArrayList(ARG_DIALOG_OPTIONS, ArrayList(options))
            args.putIntArray(ARG_DIALOG_OPTIONS_THUMBNAILS, optionsThumbnailsDrawableIds)
            args.putInt(ARG_SELECTED_OPTION_INDEX, selectedOptionIndex)
            fragment.arguments = args
            fragment.setTargetFragment(targetFragment, requestCode)
            return fragment
        }
    }
}