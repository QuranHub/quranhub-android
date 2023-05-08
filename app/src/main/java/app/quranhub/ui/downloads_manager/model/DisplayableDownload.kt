package app.quranhub.ui.downloads_manager.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DisplayableDownload {

    @NonNull
    private String name;
    @Nullable
    private String downloadedAmount;
    private boolean downloadable = true;
    private boolean deletable = false;


    public DisplayableDownload(@NonNull String name) {
        this.name = name;
    }

    public DisplayableDownload(@NonNull String name, @Nullable String downloadedAmount,
                               boolean downloadable, boolean deletable) {
        this.name = name;
        this.downloadedAmount = downloadedAmount;
        this.downloadable = downloadable;
        this.deletable = deletable;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Nullable
    public String getDownloadedAmount() {
        return downloadedAmount;
    }

    public void setDownloadedAmount(@Nullable String downloadedAmount) {
        this.downloadedAmount = downloadedAmount;
    }

    public boolean isDownloadable() {
        return downloadable;
    }

    public void setDownloadable(boolean downloadable) {
        this.downloadable = downloadable;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    @NonNull
    @Override
    public String toString() {
        return "DisplayableDownload{" +
                "name='" + name + '\'' +
                ", downloadedAmount='" + downloadedAmount + '\'' +
                ", downloadable=" + downloadable +
                ", deletable=" + deletable +
                '}';
    }
}
