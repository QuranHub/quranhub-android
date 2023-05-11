package app.quranhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Reciter {

    @NonNull
    @PrimaryKey
    private String id;

    @NonNull
    private String name;

    @NonNull
    private String nationality;

    @NonNull
    @ColumnInfo(name = "audio_base_url")
    private String audioBaseUrl;

    private Reciter() {

    }

    public Reciter(@NonNull String id, @NonNull String name, @NonNull String nationality,
                   @NonNull String audioBaseUrl) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.audioBaseUrl = audioBaseUrl;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getNationality() {
        return nationality;
    }

    public void setNationality(@NonNull String nationality) {
        this.nationality = nationality;
    }

    @NonNull
    public String getAudioBaseUrl() {
        return audioBaseUrl;
    }

    public void setAudioBaseUrl(@NonNull String audioBaseUrl) {
        this.audioBaseUrl = audioBaseUrl;
    }

    @NonNull
    @Override
    public String toString() {
        return "Reciter{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nationality='" + nationality + '\'' +
                ", audioBaseUrl='" + audioBaseUrl + '\'' +
                '}';
    }
}
