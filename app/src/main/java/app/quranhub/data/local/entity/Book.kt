package app.quranhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import app.quranhub.data.remote.model.BookContent;
import app.quranhub.ui.mushaf.adapter.BookAdapter;

@Entity
public class Book {

    @PrimaryKey
    private int id;
    private int downloadStatus;
    private long downloadId;


    public Book() {
    }

    public Book(BookContent content) {
        this.id = content.getId();
        this.downloadStatus = BookAdapter.TRANSLATION_NOT_DOWNLOADED;
        this.downloadId = -1;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }


    @NonNull
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", downloadStatus=" + downloadStatus +
                ", downloadId=" + downloadId +
                '}';
    }

}
