package app.quranhub.data.local.entity;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = BookmarkType.class,
        parentColumns = "typeId", childColumns = "bookmarkTypeId"))
public class AyaBookmark {

    @PrimaryKey
    private int bookmarkId;
    private int bookmarkTypeId;
    @Embedded
    private Aya aya;

    public AyaBookmark(int bookmarkId, int bookmarkTypeId, Aya aya) {
        this.bookmarkId = bookmarkId;
        this.bookmarkTypeId = bookmarkTypeId;
        this.aya = aya;
    }

    public int getBookmarkId() {
        return bookmarkId;
    }

    public void setBookmarkId(int bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

    public int getBookmarkTypeId() {
        return bookmarkTypeId;
    }

    public void setBookmarkTypeId(int bookmarkTypeId) {
        this.bookmarkTypeId = bookmarkTypeId;
    }

    public Aya getAya() {
        return aya;
    }

    public void setAya(Aya aya) {
        this.aya = aya;
    }

    @Override
    public String toString() {
        return "AyaBookmark{" +
                "bookmarkId=" + bookmarkId +
                ", bookmarkTypeId=" + bookmarkTypeId +
                ", aya=" + aya +
                '}';
    }

}
