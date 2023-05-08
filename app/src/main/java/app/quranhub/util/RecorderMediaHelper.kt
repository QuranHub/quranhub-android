package app.quranhub.util;


import android.media.MediaPlayer;
import android.os.Handler;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RecorderMediaHelper {

    public static final int PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 150;
    public static final int TIMER_INTERVAL_MS = 1;

    private MediaPlayer mediaPlayer;
    private MediaPlayerCallback mediaPlayerCallback;
    private ScheduledExecutorService progressExecutor;
    private Runnable seekbarPositionUpdateTask, audioTimeRunnable;
    private Handler audioUpdatedTimeTask;

    public RecorderMediaHelper() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            stopUpdatingCallbackWithPosition();
            if (mediaPlayerCallback != null) {
                mediaPlayerCallback.onStateChanged(MediaPlayerCallback.State.COMPLETED);
            }
        });
    }

    public void setMediaPlayerCallback(MediaPlayerCallback mediaPlayerCallback) {
        this.mediaPlayerCallback = mediaPlayerCallback;
    }

    public void setAudioPath(String path) {
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initProgressCallback();
    }

    private void initProgressCallback() {
        int duration = mediaPlayer.getDuration();
        if (mediaPlayerCallback != null) {
            mediaPlayerCallback.onGetMaxDuration(duration);
            mediaPlayerCallback.onPositionChanged(0);

        }
    }

    private void stopUpdatingCallbackWithPosition() {
        if (progressExecutor != null) {
            progressExecutor.shutdown();
            progressExecutor = null;
            seekbarPositionUpdateTask = null;
            stopAudioUpdatedTime();
            if (mediaPlayerCallback != null) {
                mediaPlayerCallback.onPositionChanged(0);
            }
        }
    }

    private void stopAudioUpdatedTime() {
        if (audioUpdatedTimeTask != null) {
            audioUpdatedTimeTask.removeCallbacks(audioTimeRunnable);
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopAudioUpdatedTime();
    }


    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            if (mediaPlayerCallback != null) {
                mediaPlayerCallback.onStateChanged(MediaPlayerCallback.State.PLAYING);
            }
            startUpdatingCallbackWithPosition();
        }
    }


    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (mediaPlayerCallback != null) {
                mediaPlayerCallback.onStateChanged(MediaPlayerCallback.State.PAUSED);
            }
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    /**
     * Syncs the mMediaPlayer position with Seekbar progress.
     */
    private void startUpdatingCallbackWithPosition() {

        if (progressExecutor == null) {
            progressExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (seekbarPositionUpdateTask == null) {
            seekbarPositionUpdateTask = () -> {
                if (mediaPlayer != null && mediaPlayerCallback != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    mediaPlayerCallback.onPositionChanged(currentPosition);
                }
            };
        }

        //Run a Runnable task every 1 second to update SeekBar with current recorder postion
        progressExecutor.scheduleAtFixedRate(seekbarPositionUpdateTask, 0, PLAYBACK_POSITION_REFRESH_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }


    public void startUpdatingAudioTime() {
        if (audioUpdatedTimeTask == null) {
            audioUpdatedTimeTask = new Handler();
            audioTimeRunnable = () -> milliSecondsToTimer(mediaPlayer.getCurrentPosition());
        }
        audioUpdatedTimeTask.postDelayed(audioTimeRunnable, 1000);
    }

    private void milliSecondsToTimer(int milliseconds) {
        String timerString = "";
        String secondsString = "";
        int minutes = (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000) + 1;
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        timerString += minutes + ":" + secondsString;

        if (mediaPlayerCallback != null) {
            mediaPlayerCallback.onUpdatedTime(timerString);
        }
        audioUpdatedTimeTask.postDelayed(audioTimeRunnable, 1000);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }


    public interface MediaPlayerCallback extends AyaAudioHelper.AudioStateCallback {

        void onGetMaxDuration(int duration);

        void onPositionChanged(int position);

        void onUpdatedTime(String time);

    }

}

