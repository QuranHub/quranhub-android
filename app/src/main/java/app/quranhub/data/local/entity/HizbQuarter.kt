package app.quranhub.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = Aya.class, parentColumns = "id", childColumns = "aya_from"),
        @ForeignKey(entity = Aya.class, parentColumns = "id", childColumns = "aya_to")})
public class HizbQuarter {

    @PrimaryKey
    private int id;

    @ColumnInfo(name = "aya_from")
    private int ayaFrom;

    @ColumnInfo(name = "aya_to")
    private int ayaTo;

    public HizbQuarter(int id, int ayaFrom, int ayaTo) {
        this.id = id;
        this.ayaFrom = ayaFrom;
        this.ayaTo = ayaTo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAyaFrom() {
        return ayaFrom;
    }

    public void setAyaFrom(int ayaFrom) {
        this.ayaFrom = ayaFrom;
    }

    public int getAyaTo() {
        return ayaTo;
    }

    public void setAyaTo(int ayaTo) {
        this.ayaTo = ayaTo;
    }

    @Override
    public String toString() {
        return "HizbQuarter{" +
                "id=" + id +
                ", ayaFrom=" + ayaFrom +
                ", ayaTo=" + ayaTo +
                '}';
    }
}
