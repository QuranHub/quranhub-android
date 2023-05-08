package app.quranhub.ui.mushaf.dialogs

import android.R
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import app.quranhub.databinding.DialogOpenFileBinding

class OpenFileDialog : DialogFragment() {

    private var binding: DialogOpenFileBinding? = null
    private var dialog: Dialog? = null
    private var listener: OpenFileListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OpenFileListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogOpenFileBinding.inflate(layoutInflater)
        initializeDialog()
        return dialog!!
    }

    private fun openPdfInApp() {
        dialog!!.cancel()
        listener!!.onOpenFile(IN_APP)
    }

    private fun openPdfOutApp() {
        dialog!!.cancel()
        listener!!.onOpenFile(OUT_APP)
    }

    private fun initializeDialog() {
        dialog = Dialog(requireActivity())
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(binding!!.root)
        dialog!!.window?.setBackgroundDrawableResource(R.color.transparent)
        attachListeners()
    }

    private fun attachListeners() {
        binding!!.inApp.setOnClickListener { v: View? -> openPdfInApp() }
        binding!!.outApp.setOnClickListener { v: View? -> openPdfOutApp() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    interface OpenFileListener {
        fun onOpenFile(openType: Int)
    }

    companion object {
        const val IN_APP = 1
        const val OUT_APP = 2
    }
}