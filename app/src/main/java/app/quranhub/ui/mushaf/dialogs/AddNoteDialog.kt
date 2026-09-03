package app.quranhub.ui.mushaf.dialogs

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.entity.Note
import app.quranhub.databinding.DialogAddNoteBinding
import app.quranhub.util.DialogUtils
import app.quranhub.util.DialogUtils.adjustDialogSize
import app.quranhub.util.RecorderMediaHelper
import app.quranhub.util.RecorderMediaHelper.MediaPlayerCallback
import app.quranhub.util.RecorderMediaHelper.PlaybackState
import java.io.File
import java.io.IOException

class AddNoteDialog : DialogFragment(), MediaPlayerCallback {

    private var binding: DialogAddNoteBinding? = null

    private var isRecord = false
    private var isPlaying = false
    private var isRecorderAttached = false
    private var userIsSeeking = false
    private var firstPlay = true
    private var userSelectedPosition = 0
    private var dialog: Dialog? = null
    private var listener: AddNoteListener? = null
    private var permissions = arrayOf<String>()
    private var outputRecorderPath: String? = null
    private var ayaId = 0
    private var audioRecorder: MediaRecorder? = null
    private var recorderMediaHelper: RecorderMediaHelper? = null
    private var outputFile: File? = null
    private var note: Note? = null
    private var isEditable = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as AddNoteListener?
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddNoteBinding.inflate(layoutInflater)
        initializeDialog()
        readArgs()
        listenToSeekbarChanges()
        return dialog!!
    }

    override fun onResume() {
        super.onResume()
        adjustDialogSize(
            this,
            DialogUtils.DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT,
            0.8f,
            DialogUtils.DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE,
            DialogUtils.DIALOG_STD_HEIGHT_SCREEN_RATIO_LANDSCAPE
        )
    }

    fun initializeDialog() {
        dialog = Dialog(requireActivity())
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setContentView(binding!!.root)
        dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog!!.setCanceledOnTouchOutside(false)
        permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
        attachListeners()
    }

    private fun readArgs() {
        arguments?.let {
            ayaId = it.getInt("aya_id")
            note = it.getParcelable("selected_aya")
            createRecordingFile()
            if (note != null) {
                isEditable = true
                setEditView()
                binding!!.saveBtn.text = getString(R.string.save)
            }
        }
    }

    private fun setEditView() {
        binding!!.tvTitle.text = getString(R.string.edit_note)
        (binding!!.noteTypeGroup.getChildAt(note!!.noteType) as RadioButton).isChecked = true
        if (note!!.noteText != null) {
            binding!!.addNoteEt.setText(note!!.noteText)
        }
        if (note?.noteRecorderPath?.isNotEmpty() == true) {
            setAudioViewsVisible()
            isRecorderAttached = true
            initSoundMedia()
        }
    }

    private fun setAudioViewsVisible() {
        binding!!.voiceTimerTv.visibility = View.VISIBLE
        binding!!.recordGroup.visibility = View.VISIBLE
        binding!!.addRecorderIv.visibility = View.INVISIBLE
        binding!!.voiceStatusTv.text = getString(R.string.voice_listen)
    }

    private fun createRecordingFile() {
        val file = File(
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            Constants.Directory.NOTE_VOICE_RECORDER
        )
        if (!file.exists()) {
            file.mkdir()
        }
        outputRecorderPath = file.path + File.separator + ayaId + ".3gp"
        outputFile = File(outputRecorderPath!!)
    }

    private fun attachListeners() {
        binding!!.saveBtn.setOnClickListener { onAddNote() }
        binding!!.cancelBtn.setOnClickListener { onCancel() }
        binding!!.addRecorderIv.setOnClickListener { onClickRecord() }
        binding!!.playIv.setOnClickListener { onPlayRecorder() }
        binding!!.removeRecordIv.setOnClickListener { onRemoveRecord() }
    }

    private fun onAddNote() {
        if (TextUtils.isEmpty(binding!!.addNoteEt.text) && !isRecorderAttached && !isRecord) {
            Toast.makeText(activity, getString(R.string.note_empty), Toast.LENGTH_LONG).show()
        } else {
            var path: String? = ""
            if (isRecorderAttached || isRecord) {
                path = outputRecorderPath
            } else {
                deleteRecorderFile()
            }
            val selectedType = binding!!.noteTypeGroup.indexOfChild(
                binding!!.root.findViewById(binding!!.noteTypeGroup.checkedRadioButtonId)
            )
            listener!!.onAddNote(
                Note(
                    ayaId,
                    selectedType,
                    binding!!.addNoteEt.text.toString(),
                    path
                ), isEditable
            )
            dismiss()
        }
    }

    private fun onCancel() {
        listener!!.onDismissDialog()
        dismiss()
    }

    private fun onClickRecord() {
        if (isRecord) {
            setAudioViewsVisible()
            stopTimer()
            stopRecorderMedia()
            initSoundMedia()
            isRecord = false
            isRecorderAttached = true
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(
                        requireActivity(), permissions[0]
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    initRecording()
                } else {
                    requestPermissions(permissions, 1)
                }
            } else {
                initRecording()
            }
        }
    }

    private fun initSoundMedia() {
        recorderMediaHelper = RecorderMediaHelper()
        recorderMediaHelper!!.setMediaPlayerCallback(this)
        recorderMediaHelper!!.setAudioPath(outputRecorderPath)
    }

    private fun stopRecorderMedia() {
        if (audioRecorder != null) {
            if (isRecord) {
                audioRecorder!!.stop()
            }
            audioRecorder!!.release()
        }
        if (recorderMediaHelper != null) {
            recorderMediaHelper!!.release()
        }
    }

    private fun onPlayRecorder() {
        if (isPlaying) {
            binding!!.playIv.setImageResource(R.drawable.player_play_white_ic)
            recorderMediaHelper!!.pause()
        } else {
            binding!!.playIv.setImageResource(R.drawable.ic_pause)
            recorderMediaHelper!!.play()
            recorderMediaHelper!!.startUpdatingAudioTime()
            if (firstPlay) {
                firstPlay = false
                binding!!.voiceTimerTv.text = "0:00"
            }
        }
        isPlaying = !isPlaying
    }

    private fun onRemoveRecord() {
        binding!!.recordGroup.visibility = View.GONE
        binding!!.voiceTimerTv.visibility = View.GONE
        binding!!.addRecorderIv.visibility = View.VISIBLE
        binding!!.addRecorderIv.setBackgroundResource(R.drawable.corner_primary_dialog)
        binding!!.voiceStatusTv.text = getString(R.string.add_voice)
        recorderMediaHelper!!.release()
        isRecorderAttached = false
    }

    fun deleteRecorderFile() {
        if (outputFile!!.exists()) {
            outputFile!!.delete()
        }
    }

    private fun listenToSeekbarChanges() {
        binding!!.recorderProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                userIsSeeking = true
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    userSelectedPosition = progress
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                userIsSeeking = false
                recorderMediaHelper!!.seekTo(userSelectedPosition)
            }
        })
    }

    private fun initRecording() {
        isRecord = true
        binding!!.addRecorderIv.setBackgroundResource(R.drawable.red_corner)
        binding!!.voiceStatusTv.text = getString(R.string.voice_recorded)
        startTimer()
        startRecord()
    }

    private fun startRecord() {
        audioRecorder = MediaRecorder()
        audioRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        audioRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        audioRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        audioRecorder!!.setOutputFile(outputRecorderPath)
        try {
            audioRecorder!!.prepare()
            audioRecorder!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopTimer() {
        binding!!.recorderChronometer.visibility = View.GONE
        binding!!.recorderChronometer.stop()
    }

    private fun startTimer() {
        binding!!.recorderChronometer.visibility = View.VISIBLE
        binding!!.recorderChronometer.base = SystemClock.elapsedRealtime()
        binding!!.recorderChronometer.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isGranted = true
        for (i in permissions.indices) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isGranted = false
                break
            }
        }
        if (isGranted) {
            initRecording()
        } else {
            Toast.makeText(activity, getString(R.string.accept_perm), Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener!!.onDismissDialog()
        stopRecorderMedia()
        binding = null
    }

    override fun onGetMaxDuration(duration: Int) {
        binding!!.recorderProgress.max = duration
    }

    override fun onPositionChanged(position: Int) {
        if (!userIsSeeking) {
            if (Build.VERSION.SDK_INT >= 24) {
                binding!!.recorderProgress.setProgress(position, true)
            } else {
                binding!!.recorderProgress.progress = position
            }
        }
    }

    override fun onStateChanged(state: PlaybackState) {
        if (state == PlaybackState.COMPLETED) {
            binding!!.recorderProgress.progress = 0
            isPlaying = false
            firstPlay = true
            binding!!.playIv.setImageResource(R.drawable.player_play_white_ic)
        }
    }

    override fun onUpdatedTime(time: String?) {
        binding!!.voiceTimerTv.text = time
    }

    interface AddNoteListener {
        fun onAddNote(note: Note?, isEditable: Boolean)
        fun onDismissDialog()
    }

    companion object {

        fun getInstance(ayaId: Int): AddNoteDialog {
            val bundle = Bundle()
            bundle.putInt("aya_id", ayaId)
            val dialog = AddNoteDialog()
            dialog.arguments = bundle
            return dialog
        }

        @JvmStatic
        fun getInstance(selectedAyaNote: Note): AddNoteDialog {
            val bundle = Bundle()
            bundle.putInt("aya_id", selectedAyaNote.ayaId)
            bundle.putParcelable("selected_aya", selectedAyaNote)
            val dialog = AddNoteDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}