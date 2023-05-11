package app.quranhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = ReciterRecitation.class, parentColumns = "id"
        , childColumns = "sheikh_recitation_id", onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE))
public class QuranAudio {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int page;

    private int sura;

    private int aya;

    @ColumnInfo(name = "aya_id")
    private int ayaId;

    @NonNull
    @ColumnInfo(name = "file_path")
    private String filePath;

    @ColumnInfo(name = "sheikh_recitation_id")
    private int sheikhRecitationId;

    public QuranAudio(int page, int sura, int aya, int ayaId, @NonNull String filePath, int sheikhRecitationId) {
        this.page = page;
        this.sura = sura;
        this.aya = aya;
        this.ayaId = ayaId;
        this.filePath = filePath;
        this.sheikhRecitationId = sheikhRecitationId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSura() {
        return sura;
    }

    public void setSura(int sura) {
        this.sura = sura;
    }

    public int getAya() {
        return aya;
    }

    public void setAya(int aya) {
        this.aya = aya;
    }

    public int getAyaId() {
        return ayaId;
    }

    public void setAyaId(int ayaId) {
        this.ayaId = ayaId;
    }

    @NonNull
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(@NonNull String filePath) {
        this.filePath = filePath;
    }

    public int getSheikhRecitationId() {
        return sheikhRecitationId;
    }

    public void setSheikhRecitationId(int sheikhRecitationId) {
        this.sheikhRecitationId = sheikhRecitationId;
    }

    @NonNull
    @Override
    public String toString() {
        return "QuranAudio{" +
                "id=" + id +
                ", page=" + page +
                ", sura=" + sura +
                ", aya=" + aya +
                ", ayaId=" + ayaId +
                ", filePath='" + filePath + '\'' +
                ", sheikhRecitationId=" + sheikhRecitationId +
                '}';
    }
}
