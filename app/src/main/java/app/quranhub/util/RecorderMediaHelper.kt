package app.quranhub.util

import android.media.MediaPlayer
import android.os.Handler
import app.quranhub.util.AyaAudioHelper.AudioStateCallback
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class RecorderMediaHelper {

    private var mediaPlayer: MediaPlayer?
    private var mediaPlayerCallback: MediaPlayerCallback? = null
    private var progressExecutor: ScheduledExecutorService? = null
    private var seekbarPositionUpdateTask: Runnable? = null
    private var audioTimeRunnable: Runnable? = null
    private var audioUpdatedTimeTask: Handler? = null

    init {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setOnCompletionListener { mp: MediaPlayer? ->
            stopUpdatingCallbackWithPosition()
            if (mediaPlayerCallback != null) {
                mediaPlayerCallback!!.onStateChanged(AudioStateCallback.State.COMPLETED)
            }
        }
    }

    fun setMediaPlayerCallback(mediaPlayerCallback: MediaPlayerCallback?) {
        this.mediaPlayerCallback = mediaPlayerCallback
    }

    fun setAudioPath(path: String?) {
        try {
            mediaPlayer!!.setDataSource(path)
            mediaPlayer!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        initProgressCallback()
    }

    private fun initProgressCallback() {
        val duration = mediaPlayer!!.duration
        if (mediaPlayerCallback != null) {
            mediaPlayerCallback!!.onGetMaxDuration(duration)
            mediaPlayerCallback!!.onPositionChanged(0)
        }
    }

    private fun stopUpdatingCallbackWithPosition() {
        if (progressExecutor != null) {
            progressExecutor!!.shutdown()
            progressExecutor = null
            seekbarPositionUpdateTask = null
            stopAudioUpdatedTime()
            if (mediaPlayerCallback != null) {
                mediaPlayerCallback!!.onPositionChanged(0)
            }
        }
    }

    private fun stopAudioUpdatedTime() {
        if (audioUpdatedTimeTask != null) {
            audioUpdatedTimeTask!!.removeCallbacks(audioTimeRunnable!!)
        }
    }

    fun release() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        stopAudioUpdatedTime()
    }

    fun play() {
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
            if (mediaPlayerCallback != null) {
                mediaPlayerCallback!!.onStateChanged(AudioStateCallback.State.PLAYING)
            }
            startUpdatingCallbackWithPosition()
        }
    }

    fun pause() {
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            if (mediaPlayerCallback != null) {
                mediaPlayerCallback!!.onStateChanged(AudioStateCallback.State.PAUSED)
            }
        }
    }

    fun seekTo(position: Int) {
        if (mediaPlayer != null) {
            mediaPlayer!!.seekTo(position)
        }
    }

    /**
     * Syncs the mMediaPlayer position with Seekbar progress.
     */
    private fun startUpdatingCallbackWithPosition() {
        if (progressExecutor == null) {
            progressExecutor = Executors.newSingleThreadScheduledExecutor()
        }
        if (seekbarPositionUpdateTask == null) {
            seekbarPositionUpdateTask = Runnable {
                if (mediaPlayer != null && mediaPlayerCallback != null && mediaPlayer!!.isPlaying) {
                    val currentPosition = mediaPlayer!!.currentPosition
                    mediaPlayerCallback!!.onPositionChanged(currentPosition)
                }
            }
        }

        //Run a Runnable task every 1 second to update SeekBar with current recorder position
        progressExecutor!!.scheduleAtFixedRate(
            seekbarPositionUpdateTask,
            0,
            PLAYBACK_POSITION_REFRESH_INTERVAL_MS.toLong(),
            TimeUnit.MILLISECONDS
        )
    }

    fun startUpdatingAudioTime() {
        if (audioUpdatedTimeTask == null) {
            audioUpdatedTimeTask = Handler()
            audioTimeRunnable = Runnable { milliSecondsToTimer(mediaPlayer!!.currentPosition) }
        }
        audioUpdatedTimeTask!!.postDelayed(audioTimeRunnable!!, 1000)
    }

    private fun milliSecondsToTimer(milliseconds: Int) {
        var timerString = ""
        var secondsString = ""
        val minutes = milliseconds % (1000 * 60 * 60) / (1000 * 60)
        val seconds = milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000 + 1
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        timerString += "$minutes:$secondsString"
        if (mediaPlayerCallback != null) {
            mediaPlayerCallback!!.onUpdatedTime(timerString)
        }
        audioUpdatedTimeTask!!.postDelayed(audioTimeRunnable!!, 1000)
    }

    val currentPosition: Int
        get() = mediaPlayer!!.currentPosition

    interface MediaPlayerCallback : AudioStateCallback {
        fun onGetMaxDuration(duration: Int)
        fun onPositionChanged(position: Int)
        fun onUpdatedTime(time: String?)
    }

    companion object {
        const val PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 150
        const val TIMER_INTERVAL_MS = 1
    }
}