package app.quranhub.data.local.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class BookmarkType implements Parcelable {

    @PrimaryKey
    private int typeId;
    private String bookmarkTypeName;
    @Nullable
    private int colorIndex;

    public BookmarkType(int typeId, String bookmarkTypeName) {
        this.typeId = typeId;
        this.bookmarkTypeName = bookmarkTypeName;
    }

    @Ignore
    public BookmarkType(int typeId, String bookmarkTypeName, int colorIndex) {
        this.typeId = typeId;
        this.bookmarkTypeName = bookmarkTypeName;
        this.colorIndex = colorIndex;
    }

    protected BookmarkType(Parcel in) {
        typeId = in.readInt();
        bookmarkTypeName = in.readString();
    }

    public static final Creator<BookmarkType> CREATOR = new Creator<BookmarkType>() {
        @Override
        public BookmarkType createFromParcel(Parcel in) {
            return new BookmarkType(in);
        }

        @Override
        public BookmarkType[] newArray(int size) {
            return new BookmarkType[size];
        }
    };

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getBookmarkTypeName() {
        return bookmarkTypeName;
    }

    public void setBookmarkTypeName(String bookmarkTypeName) {
        this.bookmarkTypeName = bookmarkTypeName;
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(typeId);
        dest.writeString(bookmarkTypeName);
    }

    @NonNull
    @Override
    public String toString() {
        return "BookmarkType{" +
                "typeId=" + typeId +
                ", bookmarkTypeName='" + bookmarkTypeName + '\'' +
                ", colorIndex=" + colorIndex +
                '}';
    }
}
