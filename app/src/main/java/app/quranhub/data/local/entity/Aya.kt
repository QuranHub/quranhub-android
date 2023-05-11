package app.quranhub.data.local.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {
        @ForeignKey(entity = Sura.class, parentColumns = "id", childColumns = "sura"),
        @ForeignKey(entity = Juz.class, parentColumns = "id", childColumns = "juz")})
public class Aya implements Parcelable {

    @PrimaryKey
    private int id;

    // sura number in Quran
    private int sura;

    // num of this aya in its sura
    @ColumnInfo(name = "sura_aya")
    private int suraAya;

    @NonNull
    private String text;

    @NonNull
    @ColumnInfo(name = "pure_text")
    private String pureText;

    private int page;

    private double amount;

    private int juz;

    private int x;

    private int y;

    private int xw;

    private int yw;

    @NonNull
    private String tafseer;


    public Aya(int id, int sura, int suraAya, @NonNull String text, @NonNull String pureText
            , int page, double amount, int juz, int x, int y, int xw, int yw
            , @NonNull String tafseer) {
        this.id = id;
        this.sura = sura;
        this.suraAya = suraAya;
        this.text = text;
        this.pureText = pureText;
        this.page = page;
        this.amount = amount;
        this.juz = juz;
        this.x = x;
        this.y = y;
        this.xw = xw;
        this.yw = yw;
        this.tafseer = tafseer;
    }

    protected Aya(Parcel in) {
        id = in.readInt();
        sura = in.readInt();
        suraAya = in.readInt();
        text = in.readString();
        pureText = in.readString();
        page = in.readInt();
        amount = in.readDouble();
        juz = in.readInt();
        x = in.readInt();
        y = in.readInt();
        xw = in.readInt();
        yw = in.readInt();
        tafseer = in.readString();
    }

    public static final Creator<Aya> CREATOR = new Creator<Aya>() {
        @Override
        public Aya createFromParcel(Parcel in) {
            return new Aya(in);
        }

        @Override
        public Aya[] newArray(int size) {
            return new Aya[size];
        }
    };

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

    @NonNull
    public String getText() {
        return text;
    }

    public void setText(@NonNull String text) {
        this.text = text;
    }

    @NonNull
    public String getPureText() {
        return pureText;
    }

    public void setPureText(@NonNull String pureText) {
        this.pureText = pureText;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getJuz() {
        return juz;
    }

    public void setJuz(int juz) {
        this.juz = juz;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getXw() {
        return xw;
    }

    public void setXw(int xw) {
        this.xw = xw;
    }

    public int getYw() {
        return yw;
    }

    public void setYw(int yw) {
        this.yw = yw;
    }

    @NonNull
    public String getTafseer() {
        return tafseer;
    }

    public void setTafseer(@NonNull String tafseer) {
        this.tafseer = tafseer;
    }

    @NonNull
    @Override
    public String toString() {
        return "Aya{" +
                "id=" + id +
                ", sura=" + sura +
                ", suraAya=" + suraAya +
                ", text='" + text + '\'' +
                ", pureText='" + pureText + '\'' +
                ", page=" + page +
                ", amount=" + amount +
                ", juz=" + juz +
                ", x=" + x +
                ", y=" + y +
                ", xw=" + xw +
                ", yw=" + yw +
                ", tafseer='" + tafseer + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(sura);
        dest.writeInt(suraAya);
        dest.writeString(text);
        dest.writeString(pureText);
        dest.writeInt(page);
        dest.writeDouble(amount);
        dest.writeInt(juz);
        dest.writeInt(x);
        dest.writeInt(y);
        dest.writeInt(xw);
        dest.writeInt(yw);
        dest.writeString(tafseer);
    }
}
