package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.quranhub.util.AyaAudioHelper

class AyaAudioViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaPlayer: MediaPlayer?
    private val _audioStateLiveData: MutableLiveData<Int>

    init {
        mediaPlayer = MediaPlayer()
        _audioStateLiveData = MutableLiveData()
        mediaPlayer.setOnCompletionListener(OnCompletionListener {
            _audioStateLiveData.setValue(
                AyaAudioHelper.AudioStateCallback.State.COMPLETED
            )
        })
    }

    fun setAudioPath(path: String?) {
        stopAudio()
        try {
            Log.e("TAG", path!!)
            mediaPlayer!!.setDataSource(path)
            mediaPlayer.setOnPreparedListener { mp: MediaPlayer? ->
                _audioStateLiveData.value = AyaAudioHelper.AudioStateCallback.State.PLAYING
                mediaPlayer.start()
            }
            mediaPlayer.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        mediaPlayer?.release()
    }

    val isPlaying: Boolean
        get() = try {
            mediaPlayer!!.isPlaying
        } catch (e: Exception) {
            false
        }

    fun stopAudio() {
        try {
            mediaPlayer!!.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play() {
        if (mediaPlayer != null && !isPlaying) {
            try {
                mediaPlayer.start()
                _audioStateLiveData.setValue(AyaAudioHelper.AudioStateCallback.State.PLAYING)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pause() {
        if (mediaPlayer != null && isPlaying) {
            try {
                mediaPlayer.pause()
                _audioStateLiveData.setValue(AyaAudioHelper.AudioStateCallback.State.PAUSED)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAudioStateLiveData(): LiveData<Int> {
        return _audioStateLiveData
    }
}