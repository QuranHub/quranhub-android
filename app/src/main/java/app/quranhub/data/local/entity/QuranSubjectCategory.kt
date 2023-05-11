package app.quranhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class QuranSubjectCategory {

    @PrimaryKey
    private int id;
    @NonNull
    private String name;


    public QuranSubjectCategory(int id, @NonNull String name) {
        this.id = id;
        this.name = name;
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

    @Override
    public String toString() {
        return "QuranSubjectCategory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
