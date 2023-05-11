package app.quranhub.ui.mushaf.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TopicCategory implements Parcelable {

    private String categoryName;
    private int ayaCount;
    private int categoryId;

    public TopicCategory(String categoryName, int ayaCount, int categoryId) {
        this.categoryName = categoryName;
        this.ayaCount = ayaCount;
        this.categoryId = categoryId;
    }


    protected TopicCategory(Parcel in) {
        categoryName = in.readString();
        ayaCount = in.readInt();
        categoryId = in.readInt();
    }

    public static final Creator<TopicCategory> CREATOR = new Creator<TopicCategory>() {
        @Override
        public TopicCategory createFromParcel(Parcel in) {
            return new TopicCategory(in);
        }

        @Override
        public TopicCategory[] newArray(int size) {
            return new TopicCategory[size];
        }
    };

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getAyaCount() {
        return ayaCount;
    }

    public void setAyaCount(int ayaCount) {
        this.ayaCount = ayaCount;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(categoryName);
        dest.writeInt(ayaCount);
        dest.writeInt(categoryId);
    }
}
