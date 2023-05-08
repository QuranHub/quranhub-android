package app.quranhub.ui.downloads_manager.dialogs

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
import app.quranhub.databinding.DialogConfirmationBinding
import app.quranhub.util.DialogUtils.wrapDialogHeight

/**
 * A `DialogFragment` to confirm deletion action.
 * Use the [DeleteConfirmationDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeleteConfirmationDialogFragment : DialogFragment() {

    private var title: String? = null
    private var description: String? = null
    private var deletePosition = 0
    private var binding: DialogConfirmationBinding? = null
    private var callbacks: DeleteConfirmationCallbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = if (context is DeleteConfirmationCallbacks) {
            context
        } else if (parentFragment is DeleteConfirmationCallbacks) {
            parentFragment as DeleteConfirmationCallbacks?
        } else {
            throw RuntimeException(
                "The containing fragment or activity must implement" +
                        " DeleteConfirmationDialogFragment#DeleteConfirmationCallbacks interface"
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_DIALOG_TITLE)
            description = it.getString(ARG_DIALOG_DESCRIPTION)
            deletePosition = it.getInt(ARG_DELETE_POSITION)
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
        // Inflate the layout for this fragment
        binding = DialogConfirmationBinding.inflate(inflater, container, false)
        initDialogView()
        return binding!!.root
    }

    private fun initDialogView() {
        binding!!.tvTitle.text = title
        binding!!.tvDescription.text = description
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachListeners()
    }

    private fun attachListeners() {
        binding!!.btnCancel.setOnClickListener { onCancelBtnClick() }
        binding!!.btnConfirm.setOnClickListener { onConfirmBtnClick() }
    }

    override fun onResume() {
        super.onResume()
        wrapDialogHeight(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun onCancelBtnClick() {
        dismiss()
    }

    private fun onConfirmBtnClick() {
        callbacks!!.onConfirmDelete(deletePosition)
        dismiss()
    }

    interface DeleteConfirmationCallbacks {
        fun onConfirmDelete(deletePosition: Int)
    }

    companion object {
        private val TAG = DeleteConfirmationDialogFragment::class.java.simpleName

        private const val ARG_DIALOG_TITLE = "ARG_DIALOG_TITLE"
        private const val ARG_DIALOG_DESCRIPTION = "ARG_DIALOG_DESCRIPTION"
        private const val ARG_DELETE_POSITION = "ARG_DELETE_POSITION"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param title       Dialog title.
         * @param description Dialog description.
         * @return A new instance of fragment DeleteConfirmationDialogFragment.
         */
        @JvmStatic
        fun newInstance(
            title: String?, description: String?, deletePosition: Int
        ): DeleteConfirmationDialogFragment {
            val fragment = DeleteConfirmationDialogFragment()
            val args = Bundle()
            args.putString(ARG_DIALOG_TITLE, title)
            args.putString(ARG_DIALOG_DESCRIPTION, description)
            args.putInt(ARG_DELETE_POSITION, deletePosition)
            fragment.arguments = args
            return fragment
        }
    }
}