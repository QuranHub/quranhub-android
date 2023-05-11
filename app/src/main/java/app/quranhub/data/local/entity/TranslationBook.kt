package app.quranhub.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Metadata for a downloaded translation/tafseer book.
 */
@Entity
public class TranslationBook {

    @PrimaryKey
    @NonNull
    private String id;
    @NonNull
    private String name;
    @NonNull
    private String author;
    @NonNull
    private String language;
    @NonNull
    private String fileDownloadPath;
    @NonNull
    private String databaseName;
    private int downloadStatus;
    private int downloadLevelPercentage;


    public TranslationBook(@NonNull String id, @NonNull String name, @NonNull String author
            , @NonNull String language, @NonNull String fileDownloadPath, @NonNull String databaseName
            , int downloadStatus, int downloadLevelPercentage) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.language = language;
        this.fileDownloadPath = fileDownloadPath;
        this.databaseName = databaseName;
        this.downloadStatus = downloadStatus;
        this.downloadLevelPercentage = downloadLevelPercentage;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
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
    public String getAuthor() {
        return author;
    }

    public void setAuthor(@NonNull String author) {
        this.author = author;
    }

    @NonNull
    public String getLanguage() {
        return language;
    }

    public void setLanguage(@NonNull String language) {
        this.language = language;
    }

    @NonNull
    public String getFileDownloadPath() {
        return fileDownloadPath;
    }

    public void setFileDownloadPath(@NonNull String fileDownloadPath) {
        this.fileDownloadPath = fileDownloadPath;
    }

    @NonNull
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(@NonNull String databaseName) {
        this.databaseName = databaseName;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public int getDownloadLevelPercentage() {
        return downloadLevelPercentage;
    }

    public void setDownloadLevelPercentage(int downloadLevelPercentage) {
        this.downloadLevelPercentage = downloadLevelPercentage;
    }

    @NonNull
    @Override
    public String toString() {
        return "TranslationBook{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", language='" + language + '\'' +
                ", fileDownloadPath='" + fileDownloadPath + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", downloadStatus=" + downloadStatus +
                ", downloadLevelPercentage=" + downloadLevelPercentage +
                '}';
    }
}
