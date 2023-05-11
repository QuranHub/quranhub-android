package app.quranhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Sura {

    @PrimaryKey
    private int id;

    @NonNull
    private String name;

    @NonNull
    private String tname;

    @NonNull
    private String ename;

    @NonNull
    private String type;

    private int order;

    private int ayas;


    public Sura(int id, @NonNull String name, @NonNull String tname, @NonNull String ename
            , @NonNull String type, int order, int ayas) {
        this.id = id;
        this.name = name;
        this.tname = tname;
        this.ename = ename;
        this.type = type;
        this.order = order;
        this.ayas = ayas;
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

    @NonNull
    public String getTname() {
        return tname;
    }

    public void setTname(@NonNull String tname) {
        this.tname = tname;
    }

    @NonNull
    public String getEname() {
        return ename;
    }

    public void setEname(@NonNull String ename) {
        this.ename = ename;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getAyas() {
        return ayas;
    }

    public void setAyas(int ayas) {
        this.ayas = ayas;
    }

    @NonNull
    @Override
    public String toString() {
        return "Sura{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", tname='" + tname + '\'' +
                ", ename='" + ename + '\'' +
                ", type='" + type + '\'' +
                ", order=" + order +
                ", ayas=" + ayas +
                '}';
    }
}
