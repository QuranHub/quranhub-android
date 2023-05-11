package app.quranhub.ui.mushaf.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SuraVersesNumber implements Parcelable {

    private int id;
    private int ayas;


    public SuraVersesNumber() {
    }

    protected SuraVersesNumber(Parcel in) {
        id = in.readInt();
        ayas = in.readInt();
    }

    public static final Creator<SuraVersesNumber> CREATOR = new Creator<SuraVersesNumber>() {
        @Override
        public SuraVersesNumber createFromParcel(Parcel in) {
            return new SuraVersesNumber(in);
        }

        @Override
        public SuraVersesNumber[] newArray(int size) {
            return new SuraVersesNumber[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAyas() {
        return ayas;
    }

    public void setAyas(int ayas) {
        this.ayas = ayas;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(ayas);
    }
}
