package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaRecorder
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import app.quranhub.data.Constants
import app.quranhub.data.local.prefs.AppPreferencesManager.getRecitationSetting
import java.io.File
import java.io.IOException

class VoiceRecorderViewModel(application: Application) : AndroidViewModel(application) {

    private var audioRecorder: MediaRecorder? = null

    var outputRecorderPath: String? = null
        private set

    /** True when the recorder was configured without the mic throwing (in use, no permission…). */
    var isRecorderAvailable: Boolean = false
        private set

    private fun ensureRecorder(): Boolean {
        if (audioRecorder != null) return isRecorderAvailable
        var recorder: MediaRecorder? = null
        return try {
            recorder = MediaRecorder()
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            audioRecorder = recorder
            isRecorderAvailable = true
            true
        } catch (e: RuntimeException) {
            // setAudioSource throws when the mic is in use, missing permission, or no HW.
            // Must not crash ViewModel creation (see Crashlytics: VoiceRecorderViewModel.<init>).
            e.printStackTrace()
            try {
                recorder?.release()
            } catch (ignored: RuntimeException) {
            }
            audioRecorder = null
            isRecorderAvailable = false
            false
        }
    }

    fun setAyaRecorderPath(ayaId: Int, context: Context) {
        val recitation = getRecitationSetting(context)
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            Constants.Directory.AYA_VOICE_RECORDER
        )
        val childFile = File(file.path + File.separator + recitation)
        if (!file.exists()) {
            file.mkdir()
            if (!childFile.exists()) {
                childFile.mkdir()
            }
        } else if (!childFile.exists()) {
            childFile.mkdir()
        }
        outputRecorderPath = childFile.path + File.separator + ayaId + ".3gp"
        if (!ensureRecorder()) return
        try {
            audioRecorder?.setOutputFile(outputRecorderPath)
        } catch (e: RuntimeException) {
            e.printStackTrace()
            isRecorderAvailable = false
        }
    }

    fun startRecord(): Boolean {
        if (!ensureRecorder()) return false
        return try {
            audioRecorder?.prepare()
            audioRecorder?.start()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: RuntimeException) {
            e.printStackTrace()
            false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            false
        }
    }

    fun stopRecorder() {
        try {
            audioRecorder?.stop()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun releaseRecorder() {
        try {
            audioRecorder?.release()
        } catch (e: RuntimeException) {
            e.printStackTrace()
        } finally {
            audioRecorder = null
            isRecorderAvailable = false
        }
    }

    override fun onCleared() {
        releaseRecorder()
        super.onCleared()
    }
}