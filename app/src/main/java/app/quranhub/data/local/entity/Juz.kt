package app.quranhub.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = Sura.class, parentColumns = "id", childColumns = "sura"))
public class Juz {

    @PrimaryKey
    private int id;

    private int sura;

    @ColumnInfo(name = "sura_aya")
    private int suraAya;


    public Juz(int id, int sura, int suraAya) {
        this.id = id;
        this.sura = sura;
        this.suraAya = suraAya;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSura() {
        return sura;
    }

    public void setSura(int sura) {
        this.sura = sura;
    }

    public int getSuraAya() {
        return suraAya;
    }

    public void setSuraAya(int suraAya) {
        this.suraAya = suraAya;
    }

    @Override
    public String toString() {
        return "Juz{" +
                "id=" + id +
                ", sura=" + sura +
                ", suraAya=" + suraAya +
                '}';
    }
}
