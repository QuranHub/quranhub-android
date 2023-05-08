package app.quranhub.util;

import android.media.MediaPlayer;

import java.io.IOException;

public class AyaAudioHelper {

    private MediaPlayer mediaPlayer;
    private AudioStateCallback callback;

    public AyaAudioHelper(AudioStateCallback callback) {
        this.callback = callback;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.reset();
            if (callback != null) {
                callback.onStateChanged(AudioStateCallback.State.COMPLETED);
            }
        });
    }

    public void setAudioPath(String path) {
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }
        return false;
    }

    public void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            //mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public interface AudioStateCallback {
        interface State {
            int PLAYING = 0;
            int PAUSED = 1;
            int COMPLETED = 3;
        }

        void onStateChanged(int state);
    }

}
