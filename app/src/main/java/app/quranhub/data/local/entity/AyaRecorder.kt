package app.quranhub.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AyaRecorder {

    @PrimaryKey
    private int ayaId;
    private int recitation;
    private String recorderPath;

    public AyaRecorder() {
    }

    public AyaRecorder(int ayaId, int recitation, String recorderPath) {
        this.ayaId = ayaId;
        this.recorderPath = recorderPath;
        this.recitation = recitation;
    }

    public int getRecitation() {
        return recitation;
    }

    public void setRecitation(int recitation) {
        this.recitation = recitation;
    }

    public int getAyaId() {
        return ayaId;
    }

    public void setAyaId(int ayaId) {
        this.ayaId = ayaId;
    }

    public String getRecorderPath() {
        return recorderPath;
    }

    public void setRecorderPath(String recorderPath) {
        this.recorderPath = recorderPath;
    }
}
