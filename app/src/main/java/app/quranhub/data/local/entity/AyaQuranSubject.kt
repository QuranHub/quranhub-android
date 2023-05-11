package app.quranhub.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = Aya.class, parentColumns = "id", childColumns = "aya"),
        @ForeignKey(entity = QuranSubject.class, parentColumns = "id", childColumns = "subject")})
public class AyaQuranSubject {

    @PrimaryKey
    private int id;
    private int subject;
    private int aya;

    public AyaQuranSubject(int id, int subject, int aya) {
        this.id = id;
        this.subject = subject;
        this.aya = aya;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubject() {
        return subject;
    }

    public void setSubject(int subject) {
        this.subject = subject;
    }

    public int getAya() {
        return aya;
    }

    public void setAya(int aya) {
        this.aya = aya;
    }

    @Override
    public String toString() {
        return "AyaQuranSubject{" +
                "id=" + id +
                ", subject=" + subject +
                ", aya=" + aya +
                '}';
    }
}
