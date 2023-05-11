package app.quranhub.ui.mushaf.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.quranhub.data.local.entity.TranslationBook;

public class DisplayableTranslation {

    @NonNull
    private TranslationBook translationBook;


    public DisplayableTranslation(@NonNull TranslationBook translationBook) {
        this.translationBook = new TranslationBook(translationBook.getId(), translationBook.getName()
                , translationBook.getAuthor(), translationBook.getLanguage(), translationBook.getFileDownloadPath()
                , translationBook.getDatabaseName(), translationBook.getDownloadStatus(), translationBook.getDownloadLevelPercentage());
    }

    public DisplayableTranslation(@NonNull String id, @NonNull String name, @NonNull String author
            , @NonNull String language, @NonNull String fileDownloadPath, @NonNull String databaseName
            , int downloadStatus, int downloadLevelPercentage) {
        translationBook = new TranslationBook(id, name, author, language, fileDownloadPath
                , databaseName, downloadStatus, downloadLevelPercentage);
    }

    @NonNull
    public TranslationBook getTranslationBook() {
        return translationBook;
    }

    public void setTranslationBook(@NonNull TranslationBook translationBook) {
        this.translationBook = translationBook;
    }

    @NonNull
    public String getId() {
        return translationBook.getId();
    }

    public void setId(@NonNull String id) {
        translationBook.setId(id);
    }

    @NonNull
    public String getName() {
        return translationBook.getName();
    }

    public void setName(@NonNull String name) {
        translationBook.setName(name);
    }

    @NonNull
    public String getAuthor() {
        return translationBook.getAuthor();
    }

    public void setAuthor(@NonNull String author) {
        translationBook.setAuthor(author);
    }

    @NonNull
    public String getLanguage() {
        return translationBook.getLanguage();
    }

    public void setLanguage(@NonNull String language) {
        translationBook.setLanguage(language);
    }

    @NonNull
    public String getFileDownloadPath() {
        return translationBook.getFileDownloadPath();
    }

    public void setFileDownloadPath(@NonNull String fileDownloadPath) {
        translationBook.setFileDownloadPath(fileDownloadPath);
    }

    @NonNull
    public String getDatabaseName() {
        return translationBook.getDatabaseName();
    }

    public void setDatabaseName(@NonNull String databaseName) {
        translationBook.setDatabaseName(databaseName);
    }

    public int getDownloadStatus() {
        return translationBook.getDownloadStatus();
    }

    public void setDownloadStatus(int downloadStatus) {
        translationBook.setDownloadStatus(downloadStatus);
    }

    public int getDownloadLevelPercentage() {
        return translationBook.getDownloadLevelPercentage();
    }

    public void setDownloadLevelPercentage(int downloadLevelPercentage) {
        translationBook.setDownloadLevelPercentage(downloadLevelPercentage);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof DisplayableTranslation) {
            return this.getId().equals(((DisplayableTranslation) obj).getId());
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return "DisplayableTranslation{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", language='" + getLanguage() + '\'' +
                ", fileDownloadPath='" + getFileDownloadPath() + '\'' +
                ", databaseName='" + getDatabaseName() + '\'' +
                ", downloadStatus=" + getDownloadStatus() +
                ", downloadLevelPercentage=" + getDownloadLevelPercentage() +
                '}';
    }
}
