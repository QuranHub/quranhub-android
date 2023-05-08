package app.quranhub.ui.mushaf.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import app.quranhub.R
import app.quranhub.databinding.DialogAyaRecorderBinding
import app.quranhub.ui.mushaf.viewmodel.VoiceRecorderViewModel

class AyaRecorderDialog : DialogFragment() {

    private var dialog: Dialog? = null
    private var listener: StopRecordingListener? = null
    private var ayaId = 0
    private var voiceRecorderViewModel: VoiceRecorderViewModel? = null
    private var binding: DialogAyaRecorderBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? StopRecordingListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAyaRecorderBinding.inflate(layoutInflater)
        readArgs()
        initializeDialog()
        initReorder(savedInstanceState == null)
        getPrevState(savedInstanceState)
        return dialog!!
    }

    private fun getPrevState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            startTimer(SystemClock.elapsedRealtime())
        } else {
            startTimer(SystemClock.elapsedRealtime() + savedInstanceState.getLong("chronometer_time"))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(
            "chronometer_time",
            binding!!.recorderChronometer.base - SystemClock.elapsedRealtime()
        )
    }

    private fun initReorder(startRecord: Boolean) {
        voiceRecorderViewModel = ViewModelProvider(this).get(
            VoiceRecorderViewModel::class.java
        )
        if (startRecord) {
            voiceRecorderViewModel!!.setAyaRecorderPath(ayaId, activity)
            voiceRecorderViewModel!!.startRecord()
        }
    }

    private fun readArgs() {
        arguments?.let {
            ayaId = it.getInt(ARG_AYA_ID)
        }
    }

    fun initializeDialog() {
        dialog = Dialog(requireActivity())
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCanceledOnTouchOutside(false)
        val layoutParams = dialog!!.window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        dialog!!.setContentView(binding!!.root)
        dialog!!.window?.setBackgroundDrawableResource(R.color.transparent_color)
        attachListeners()
    }

    private fun startTimer(base: Long) {
        binding!!.recorderChronometer.base = base
        binding!!.recorderChronometer.start()
    }

    private fun attachListeners() {
        binding!!.stopRecordingView.setOnClickListener { v: View? -> onStopRecording() }
    }

    private fun onStopRecording() {
        voiceRecorderViewModel!!.releaseRecorder()
        binding!!.recorderChronometer.stop()
        listener!!.onStopRecording(voiceRecorderViewModel!!.outputRecorderPath)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding!!.recorderChronometer.stop()
        if (!requireActivity().isChangingConfigurations) {
            voiceRecorderViewModel!!.releaseRecorder()
        }
        binding = null
    }

    interface StopRecordingListener {
        fun onStopRecording(filePath: String?)
    }

    companion object {

        private const val ARG_AYA_ID = "ARG_AYA_ID"

        @JvmStatic
        fun getInstance(ayaId: Int): AyaRecorderDialog {
            val bundle = Bundle()
            bundle.putInt(ARG_AYA_ID, ayaId)
            val recorderDialog = AyaRecorderDialog()
            recorderDialog.arguments = bundle
            return recorderDialog
        }
    }
}