package app.quranhub.ui.mushaf.viewmodel;

import android.app.Application;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import app.quranhub.util.AyaAudioHelper;

public class AyaAudioViewModel extends AndroidViewModel {

    private MediaPlayer mediaPlayer;
    private MutableLiveData<Integer> audioStateLiveData;

    public AyaAudioViewModel(@NonNull Application application) {
        super(application);
        mediaPlayer = new MediaPlayer();
        audioStateLiveData = new MutableLiveData<>();
        mediaPlayer.setOnCompletionListener(mp -> {
            audioStateLiveData.setValue(AyaAudioHelper.AudioStateCallback.State.COMPLETED);
        });
    }

    public void setAudioPath(String path) {
        stopAudio();
        try {
            Log.e("TAG", path);
            mediaPlayer.setDataSource(path);
            mediaPlayer.setOnPreparedListener(mp -> {
                audioStateLiveData.setValue(AyaAudioHelper.AudioStateCallback.State.PLAYING);
                mediaPlayer.start();

            });
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public boolean isPlaying() {
        try {
            return mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public void stopAudio() {
        try {
            mediaPlayer.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (mediaPlayer != null && !isPlaying()) {
            try {
                mediaPlayer.start();
                audioStateLiveData.setValue(AyaAudioHelper.AudioStateCallback.State.PLAYING);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    public void pause() {
        if (mediaPlayer != null && isPlaying()) {
            try {
                mediaPlayer.pause();
                audioStateLiveData.setValue(AyaAudioHelper.AudioStateCallback.State.PAUSED);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public LiveData<Integer> getAudioStateLiveData() {
        return audioStateLiveData;
    }
}
