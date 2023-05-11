package app.quranhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = QuranSubjectCategory.class, parentColumns = "id", childColumns = "category")})
public class QuranSubject {

    @PrimaryKey
    private int id;
    @NonNull
    private String name;
    private int category;
    @ColumnInfo(name = "aya_count")
    private int ayaCount;

    public QuranSubject(int id, @NonNull String name, int category, int ayaCount) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.ayaCount = ayaCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public int getAyaCount() {
        return ayaCount;
    }

    public void setAyaCount(int ayaCount) {
        this.ayaCount = ayaCount;
    }

    @Override
    public String toString() {
        return "QuranSubject{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", ayaCount=" + ayaCount +
                '}';
    }
}
