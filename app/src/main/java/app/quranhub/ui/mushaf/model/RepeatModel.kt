package app.quranhub.ui.mushaf.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RepeatModel implements Parcelable {

    private int fromSura;
    private int fromAya;
    private int fromAyaId;
    private int toSura;
    private int toAya;
    private int toAyaId;
    private int groupRepeatNum;
    private int ayaRepeatNum;
    private int delayTime;

    public RepeatModel() {
    }

    protected RepeatModel(Parcel in) {
        fromSura = in.readInt();
        fromAya = in.readInt();
        toSura = in.readInt();
        toAya = in.readInt();
        groupRepeatNum = in.readInt();
        ayaRepeatNum = in.readInt();
        delayTime = in.readInt();
    }


    public static final Creator<RepeatModel> CREATOR = new Creator<RepeatModel>() {
        @Override
        public RepeatModel createFromParcel(Parcel in) {
            return new RepeatModel(in);
        }

        @Override
        public RepeatModel[] newArray(int size) {
            return new RepeatModel[size];
        }
    };

    public int getFromSura() {
        return fromSura;
    }

    public void setFromSura(int fromSura) {
        this.fromSura = fromSura;
    }

    public int getFromAya() {
        return fromAya;
    }

    public void setFromAya(int fromAya) {
        this.fromAya = fromAya;
    }

    public int getToSura() {
        return toSura;
    }

    public void setToSura(int toSura) {
        this.toSura = toSura;
    }

    public int getToAya() {
        return toAya;
    }

    public void setToAya(int toAya) {
        this.toAya = toAya;
    }

    public int getGroupRepeatNum() {
        return groupRepeatNum;
    }

    public void setGroupRepeatNum(int groupRepeatNum) {
        this.groupRepeatNum = groupRepeatNum;
    }

    public int getAyaRepeatNum() {
        return ayaRepeatNum;
    }

    public void setAyaRepeatNum(int ayaRepeatNum) {
        this.ayaRepeatNum = ayaRepeatNum;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public int getFromAyaId() {
        return fromAyaId;
    }

    public void setFromAyaId(int fromAyaId) {
        this.fromAyaId = fromAyaId;
    }

    public int getToAyaId() {
        return toAyaId;
    }

    public void setToAyaId(int toAyaId) {
        this.toAyaId = toAyaId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fromSura);
        dest.writeInt(fromAya);
        dest.writeInt(fromAyaId);
        dest.writeInt(toSura);
        dest.writeInt(toAya);
        dest.writeInt(toAyaId);
        dest.writeInt(groupRepeatNum);
        dest.writeInt(ayaRepeatNum);
        dest.writeInt(delayTime);
    }
}
