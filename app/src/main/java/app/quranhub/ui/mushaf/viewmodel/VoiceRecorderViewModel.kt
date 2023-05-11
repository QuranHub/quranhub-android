package app.quranhub.ui.mushaf.viewmodel;

import android.app.Application;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.io.File;
import java.io.IOException;

import app.quranhub.data.Constants;
import app.quranhub.data.local.prefs.AppPreferencesManager;

public class VoiceRecorderViewModel extends AndroidViewModel {

    private MediaRecorder audioRecorder;
    private String outputRecorderPath;

    public VoiceRecorderViewModel(@NonNull Application application) {
        super(application);
        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
    }


    public String getOutputRecorderPath() {
        return outputRecorderPath;
    }

    public void setAyaRecorderPath(int ayaId, Context context) {
        int recitation = AppPreferencesManager.getRecitationSetting(context);
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), Constants.Directory.AYA_VOICE_RECORDER);
        File childFile = new File(file.getPath() + File.separator + recitation);
        if (!file.exists()) {
            file.mkdir();
            if (!childFile.exists()) {
                childFile.mkdir();
            }
        } else if (!childFile.exists()) {
            childFile.mkdir();
        }
        outputRecorderPath = childFile.getPath() + File.separator + ayaId + ".3gp";
        audioRecorder.setOutputFile(outputRecorderPath);
    }

    public void startRecord() {
        try {
            audioRecorder.prepare();
            audioRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecorder() {
        audioRecorder.stop();
    }

    public void releaseRecorder() {
        audioRecorder.release();
    }
}
