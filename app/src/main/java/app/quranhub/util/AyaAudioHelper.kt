package app.quranhub.util

import android.media.MediaPlayer
import java.io.IOException

class AyaAudioHelper(private val callback: AudioStateCallback? = null) {

    private var mediaPlayer: MediaPlayer?

    init {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setOnCompletionListener {
            mediaPlayer!!.reset()
            callback?.onStateChanged(AudioStateCallback.State.COMPLETED)
        }
    }

    fun setAudioPath(path: String?) {
        try {
            mediaPlayer!!.setDataSource(path)
            mediaPlayer!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun release() {
        mediaPlayer?.let {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

    val isPlaying: Boolean
        get() = if (mediaPlayer != null) {
            mediaPlayer!!.isPlaying
        } else false

    fun stopAudio() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            //mediaPlayer.stop();
            mediaPlayer!!.reset()
        }
    }

    fun play() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
        }
    }

    fun pause() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
        }
    }

    interface AudioStateCallback {
        interface State {
            companion object {
                const val PLAYING = 0
                const val PAUSED = 1
                const val COMPLETED = 3
            }
        }

        fun onStateChanged(state: Int)
    }
}