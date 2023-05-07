package app.quranhub.prdownloader_service;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Information about a file download request.
 * <p>
 * Use {@link DownloadRequestInfo.Builder} to create instances
 * of this class.
 *
 * @author Abdallah Abdelazim <a href="mailto:abdallah.abdelazim@hotmail.com">abdallah.abdelazim@hotmail.com</a>
 * TODO provide & review JavaDoc documentation
 */
public class DownloadRequestInfo implements Parcelable {

    @NonNull
    protected String url;
    protected boolean isUrlRelative;

    @Nullable
    protected String dirPath;
    @Nullable
    protected String fileName;
    protected boolean shouldRetryOnFailure = true;
    @Nullable
    protected Bundle extraInfo;

    protected DownloadRequestInfo(Builder builder) {
        this.url = builder.url;
        this.isUrlRelative = builder.isUrlRelative;
        this.dirPath = builder.dirPath;
        this.fileName = builder.fileName;
        this.shouldRetryOnFailure = builder.shouldRetryOnFailure;
        this.extraInfo = builder.extraInfo;
    }

    protected DownloadRequestInfo(Parcel in) {
        url = in.readString();
        isUrlRelative = in.readByte() != 0;
        dirPath = in.readString();
        fileName = in.readString();
        shouldRetryOnFailure = in.readByte() != 0;
        extraInfo = in.readBundle(getClass().getClassLoader());
    }

    public static final Creator<DownloadRequestInfo> CREATOR = new Creator<DownloadRequestInfo>() {
        @Override
        public DownloadRequestInfo createFromParcel(Parcel in) {
            return new DownloadRequestInfo(in);
        }

        @Override
        public DownloadRequestInfo[] newArray(int size) {
            return new DownloadRequestInfo[size];
        }
    };

    @NonNull
    public String getUrl() {
        return url;
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
    }

    public boolean isUrlRelative() {
        return isUrlRelative;
    }

    public void setUrlRelative(boolean isUrlRelative) {
        this.isUrlRelative = isUrlRelative;
    }

    @Nullable
    public String getDirPath() {
        return dirPath;
    }

    /**
     * @param dirPath The absolute path of the directory in which to put the downloaded file.
     *                <p>
     *                If passed {@code null}, the file will be downloaded to the directory specified
     *                at {@link PRDownloaderService#init(String, String, String)}.
     */
    public void setDirPath(@Nullable String dirPath) {
        this.dirPath = dirPath;
    }

    @Nullable
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName A name for the downloaded file.
     *                 <p>
     *                 If passed {@code null}, the file will be named with its name in the download URL.
     */
    public void setFileName(@Nullable String fileName) {
        this.fileName = fileName;
    }

    public boolean isShouldRetryOnFailure() {
        return shouldRetryOnFailure;
    }

    public void setShouldRetryOnFailure(boolean shouldRetryOnFailure) {
        this.shouldRetryOnFailure = shouldRetryOnFailure;
    }

    @Nullable
    public Bundle getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(@Nullable Bundle extraInfo) {
        this.extraInfo = extraInfo;
    }

    @Override
    public String toString() {
        return "DownloadRequestInfo{" +
                "url='" + url + '\'' +
                ", isUrlRelative=" + isUrlRelative +
                ", dirPath='" + dirPath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", shouldRetryOnFailure=" + shouldRetryOnFailure +
                ", extraInfo=" + extraInfo +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeByte((byte) (isUrlRelative ? 1 : 0));
        dest.writeString(dirPath);
        dest.writeString(fileName);
        dest.writeByte((byte) (shouldRetryOnFailure ? 1 : 0));
        dest.writeBundle(extraInfo);
    }


    /**
     * Builder for {@link DownloadRequestInfo} objects.
     */
    public static class Builder {

        @NonNull
        protected String url;
        protected boolean isUrlRelative;

        @Nullable
        protected String dirPath;
        @Nullable
        protected String fileName;
        protected boolean shouldRetryOnFailure = true;
        @Nullable
        protected Bundle extraInfo;

        /**
         * Create a {@code Builder} with a download URL. You can specify whether this URL is
         * <em>relative</em> or <em>absolute</em>.
         * <p>
         * Using relative URLs is generally preferred if you download multiple files that exists on the
         * same server and share the same base URL. If using relative URLs, the base URL must be provided
         * to {@link PRDownloaderService#init(String, String, String)}
         *
         * @param url           The file download URL.
         * @param isUrlRelative Whether the {@code url} that you have provided is relative or absolute.
         */
        public Builder(@NonNull String url, boolean isUrlRelative) {
            this.url = url;
            this.isUrlRelative = isUrlRelative;
        }

        public Builder setDirPath(@Nullable String dirPath) {
            this.dirPath = dirPath;
            return this;
        }

        public Builder setFileName(@Nullable String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setShouldRetryOnFailure(boolean shouldRetryOnFailure) {
            this.shouldRetryOnFailure = shouldRetryOnFailure;
            return this;
        }

        public Builder setExtraInfo(@Nullable Bundle extraInfo) {
            this.extraInfo = extraInfo;
            return this;
        }

        public DownloadRequestInfo build() {
            return new DownloadRequestInfo(this);
        }

    }
}
