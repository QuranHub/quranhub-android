package app.quranhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "translation")
public class Translation {

    @PrimaryKey
    private int index;
    private int sura;
    private int aya;
    @NonNull
    private String text;

    public Translation(int sura, int aya, @NonNull String text) {
        this.sura = sura;
        this.aya = aya;
        this.text = text;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
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

    @NonNull
    public String getText() {
        return text;
    }

    public void setText(@NonNull String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return "Translation{" +
                "index=" + index +
                ", sura=" + sura +
                ", aya=" + aya +
                ", text='" + text + '\'' +
                '}';
    }

}