package app.quranhub.prdownloader_service

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

/**
 * Information about a file download request.
 *
 *
 * Use [DownloadRequestInfo.Builder] to create instances
 * of this class.
 *
 * @author Abdallah Abdelazim [abdallah.abdelazim@hotmail.com](mailto:abdallah.abdelazim@hotmail.com)
 * TODO provide & review JavaDoc documentation
 */
class DownloadRequestInfo : Parcelable {

    var url: String
    var isUrlRelative: Boolean

    /**
     * @param dirPath The absolute path of the directory in which to put the downloaded file.
     *
     *
     * If passed `null`, the file will be downloaded to the directory specified
     * at [PRDownloaderService.init].
     */
    @JvmField
    var dirPath: String?

    /**
     * @param fileName A name for the downloaded file.
     *
     *
     * If passed `null`, the file will be named with its name in the download URL.
     */
    @JvmField
    var fileName: String?
    var isShouldRetryOnFailure = true
    var extraInfo: Bundle?

    private constructor(builder: Builder) {
        url = builder.url
        isUrlRelative = builder.isUrlRelative
        dirPath = builder.dirPath
        fileName = builder.fileName
        isShouldRetryOnFailure = builder.shouldRetryOnFailure
        extraInfo = builder.extraInfo
    }

    private constructor(`in`: Parcel) {
        url = `in`.readString()!!
        isUrlRelative = `in`.readByte().toInt() != 0
        dirPath = `in`.readString()
        fileName = `in`.readString()
        isShouldRetryOnFailure = `in`.readByte().toInt() != 0
        extraInfo = `in`.readBundle(javaClass.classLoader)
    }

    override fun toString(): String {
        return "DownloadRequestInfo{" +
                "url='" + url + '\'' +
                ", isUrlRelative=" + isUrlRelative +
                ", dirPath='" + dirPath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", shouldRetryOnFailure=" + isShouldRetryOnFailure +
                ", extraInfo=" + extraInfo +
                '}'
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(url)
        dest.writeByte((if (isUrlRelative) 1 else 0).toByte())
        dest.writeString(dirPath)
        dest.writeString(fileName)
        dest.writeByte((if (isShouldRetryOnFailure) 1 else 0).toByte())
        dest.writeBundle(extraInfo)
    }

    /**
     * Builder for [DownloadRequestInfo] objects.
     *
     * Create a `Builder` with a download URL. You can specify whether this URL is
     * *relative* or *absolute*.
     *
     *
     * Using relative URLs is generally preferred if you download multiple files that exists on the
     * same server and share the same base URL. If using relative URLs, the base URL must be provided
     * to [PRDownloaderService.init]
     *
     * @param url           The file download URL.
     * @param isUrlRelative Whether the `url` that you have provided is relative or absolute.
     */
    class Builder(var url: String, var isUrlRelative: Boolean) {
        var dirPath: String? = null
        var fileName: String? = null
        var shouldRetryOnFailure = true
        var extraInfo: Bundle? = null
        fun setDirPath(dirPath: String?): Builder {
            this.dirPath = dirPath
            return this
        }

        fun setFileName(fileName: String?): Builder {
            this.fileName = fileName
            return this
        }

        fun setShouldRetryOnFailure(shouldRetryOnFailure: Boolean): Builder {
            this.shouldRetryOnFailure = shouldRetryOnFailure
            return this
        }

        fun setExtraInfo(extraInfo: Bundle?): Builder {
            this.extraInfo = extraInfo
            return this
        }

        fun build(): DownloadRequestInfo {
            return DownloadRequestInfo(this)
        }
    }

    companion object {
        @JvmField
        val CREATOR: Creator<DownloadRequestInfo?> = object : Creator<DownloadRequestInfo?> {
            override fun createFromParcel(`in`: Parcel): DownloadRequestInfo {
                return DownloadRequestInfo(`in`)
            }

            override fun newArray(size: Int): Array<DownloadRequestInfo?> {
                return arrayOfNulls(size)
            }
        }
    }
}