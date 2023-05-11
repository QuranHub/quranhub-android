package app.quranhub.data.remote.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.data.local.entity.TranslationBook;
import app.quranhub.util.NetworkUtil;

public class TranslationsResponse {

    @Nullable
    private List<TranslationData> translations;

    public TranslationsResponse(@Nullable List<TranslationData> translations) {
        this.translations = translations;
    }

    @Nullable
    public List<TranslationData> getTranslations() {
        return translations;
    }

    public void setTranslations(@Nullable List<TranslationData> translations) {
        this.translations = translations;
    }

    @Nullable
    public List<TranslationBook> getTranslationBooks() {
        return getTranslationBooksForLanguage(null);
    }

    @Nullable
    public List<TranslationBook> getTranslationBooksForLanguage(@Nullable String languageCode) {
        List<TranslationBook> translationBooks = null;
        if (translations != null) {
            translationBooks = new ArrayList<>();
            for (TranslationData t : translations) {
                if (languageCode == null || t.getLanguage().toLowerCase().equals(languageCode.toLowerCase())) {
                    TranslationBook book = new TranslationBook(t.getId(), t.getName(), t.getAuthor()
                            , t.getLanguage(), t.getPath(), t.getPath().replace('/', '_')
                            , NetworkUtil.STATUS_NOT_DOWNLOADED, 0);
                    translationBooks.add(book);
                }
            }
        }
        return translationBooks;
    }

    @NonNull
    @Override
    public String toString() {
        return "TranslationsResponse{" +
                "translations=" + translations +
                '}';
    }


    public static class TranslationData {

        @NonNull
        private String id;
        @NonNull
        private String name;
        @NonNull
        private String path;
        @NonNull
        private String author;
        @NonNull
        private String language;
        @SerializedName("created_at")
        private long createdAt;
        @SerializedName("updated_at")
        private long updatedAt;

        public TranslationData(@NonNull String id, @NonNull String name, @NonNull String path, @NonNull String author
                , @NonNull String language, long createdAt, long updatedAt) {
            this.id = id;
            this.name = name;
            this.path = path;
            this.author = author;
            this.language = language;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
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
        public String getPath() {
            return path;
        }

        public void setPath(@NonNull String path) {
            this.path = path;
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

        public long getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
        }

        public long getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
        }

        @NonNull
        @Override
        public String toString() {
            return "TranslationData{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", path='" + path + '\'' +
                    ", author='" + author + '\'' +
                    ", language='" + language + '\'' +
                    ", createdAt=" + createdAt +
                    ", updatedAt=" + updatedAt +
                    '}';
        }
    }

}
