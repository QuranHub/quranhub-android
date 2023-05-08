package app.quranhub.ui.mushaf.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.databinding.DialogPlayAyaRecorderBinding
import app.quranhub.util.AyaAudioHelper
import app.quranhub.util.RecorderMediaHelper
import app.quranhub.util.RecorderMediaHelper.MediaPlayerCallback
import java.io.File

class AyaRecorderPlayerDialog : DialogFragment(), MediaPlayerCallback {

    private var dialog: Dialog? = null
    private var listener: AyaRecorderPlayerListener? = null
    private var recorderMediaHelper: RecorderMediaHelper? = null
    private var outputRecorderPath: String? = null
    private var ayaId = 0
    private var isPlaying = false
    private var userIsSeeking = false
    private var firstPlay = true
    private var userSelectedPosition = 0
    private var binding: DialogPlayAyaRecorderBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as AyaRecorderPlayerListener?
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogPlayAyaRecorderBinding.inflate(layoutInflater)
        initializeDialog()
        setRecordingFile()
        initSoundMedia()
        getPrevState(savedInstanceState)
        listenToSeekbarChanges()
        return dialog!!
    }

    private fun getPrevState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            recorderMediaHelper!!.seekTo(savedInstanceState.getInt("player_position"))
            isPlaying = savedInstanceState.getBoolean("is_playing")
            restorePlayingState()
        }
    }

    private fun restorePlayingState() {
        if (isPlaying) {
            binding!!.playIv.setImageResource(R.drawable.ic_pause)
            recorderMediaHelper!!.play()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("is_playing", isPlaying)
        outState.putInt("player_position", recorderMediaHelper!!.currentPosition)
    }

    fun initializeDialog() {
        dialog = Dialog(requireActivity())
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        val layoutParams = dialog!!.window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        dialog!!.setContentView(binding!!.root)
        dialog!!.window?.setBackgroundDrawableResource(android.R.color.transparent)
        arguments?.let {
            ayaId = it.getInt(ARG_AYA_ID)
        }
        attachListeners()
    }

    private fun setRecordingFile() {
        val recitation = AppPreferencesManager.getRecitationSetting(requireActivity())
        val file = File(
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            Constants.Directory.AYA_VOICE_RECORDER
                    + File.separator + recitation + File.separator
                    + ayaId + ".3gp"
        )
        if (file.exists()) {
            outputRecorderPath = file.path
        } else {
            listener!!.onClickDeleteRecorder()
            Toast.makeText(activity, getString(R.string.file_not_exist), Toast.LENGTH_LONG).show()
            dismiss()
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

    private fun initSoundMedia() {
        if (outputRecorderPath != null) {
            recorderMediaHelper = RecorderMediaHelper()
            recorderMediaHelper!!.setMediaPlayerCallback(this)
            recorderMediaHelper!!.setAudioPath(outputRecorderPath)
        }
    }

    private fun attachListeners() {
        binding!!.removeRecordIv.setOnClickListener { v: View? -> onRemoveRecorder() }
        binding!!.playIv.setOnClickListener { v: View? -> onPlayRecorder() }
    }

    private fun onRemoveRecorder() {
        recorderMediaHelper!!.release()
        listener!!.onClickDeleteRecorder()
        dismiss()
    }

    fun onPlayRecorder() {
        if (isPlaying) {
            binding!!.playIv.setImageResource(R.drawable.player_play_white_ic)
            recorderMediaHelper!!.pause()
        } else {
            binding!!.playIv.setImageResource(R.drawable.ic_pause)
            recorderMediaHelper!!.play()
            recorderMediaHelper!!.startUpdatingAudioTime()
            if (firstPlay) {
                firstPlay = false
                binding!!.recorderTimeTv.text = "0:00"
            }
        }
        isPlaying = !isPlaying
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

    override fun onUpdatedTime(time: String?) {
        binding!!.recorderTimeTv.text = time
    }

    override fun onStateChanged(state: Int) {
        if (state == AyaAudioHelper.AudioStateCallback.State.COMPLETED) {
            binding!!.recorderProgress.progress = 0
            isPlaying = false
            firstPlay = true
            binding!!.playIv.setImageResource(R.drawable.player_play_white_ic)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!requireActivity().isChangingConfigurations && recorderMediaHelper != null) {
            recorderMediaHelper!!.release()
        }
        binding = null
    }

    interface AyaRecorderPlayerListener {
        fun onClickDeleteRecorder()
    }

    companion object {
        private const val ARG_AYA_ID = "ARG_AYA_ID"

        @JvmStatic
        fun getInstance(ayaId: Int): AyaRecorderPlayerDialog {
            val bundle = Bundle()
            bundle.putInt(ARG_AYA_ID, ayaId)
            val dialog = AyaRecorderPlayerDialog()
            dialog.arguments = bundle
            return dialog
        }
    }
}