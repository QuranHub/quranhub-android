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

    private val audioRecorder: MediaRecorder = MediaRecorder()

    var outputRecorderPath: String? = null
        private set

    init {
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
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
        audioRecorder.setOutputFile(outputRecorderPath)
    }

    fun startRecord() {
        try {
            audioRecorder.prepare()
            audioRecorder.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecorder() {
        audioRecorder.stop()
    }

    fun releaseRecorder() {
        audioRecorder.release()
    }
}