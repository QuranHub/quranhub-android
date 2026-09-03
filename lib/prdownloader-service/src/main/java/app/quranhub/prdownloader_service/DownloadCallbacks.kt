package app.quranhub.prdownloader_service

import com.downloader.Error
import com.downloader.Progress

/**
 * Various callbacks for `PRDownloaderService`.
 *
 *
 * Do not call any of these methods directly.
 *
 * @author Abdallah Abdelazim [abdallah.abdelazim@hotmail.com](mailto:abdallah.abdelazim@hotmail.com)
 * @see PRDownloaderService
 */
internal interface DownloadCallbacks {

    /**
     * Called when the service is being created, before any download request starts.
     *
     * This method is called only once across the lifetime of the service. You can use it for any
     * initialization.
     * Override this method instead of [android.app.Service.onCreate]
     */
    fun onStart()

    /**
     * Called when a download request is being started or resumed.
     *
     * @param downloadRequestInfo The `DownloadRequestInfo` for which this callback was called.
     */
    fun onDownloadStartOrResume(downloadRequestInfo: DownloadRequestInfo)

    /**
     * Called when a download request is being paused.
     *
     * @param downloadRequestInfo The `DownloadRequestInfo` for which this callback was called.
     */
    fun onDownloadPause(downloadRequestInfo: DownloadRequestInfo)

    /**
     * Called when a download request is being cancelled.
     *
     * @param downloadRequestInfo The `DownloadRequestInfo` for which this callback was called.
     */
    fun onDownloadCancel(downloadRequestInfo: DownloadRequestInfo)

    /**
     * Called as a download request progress is updating.
     *
     * @param downloadRequestInfo The `DownloadRequestInfo` for which this callback was called.
     * @param progress            Information about the progress.
     */
    fun onDownloadProgress(downloadRequestInfo: DownloadRequestInfo, progress: Progress)

    /**
     * Called when a download request has finished downloading successfully.
     *
     * @param downloadRequestInfo The `DownloadRequestInfo` for which this callback was called.
     */
    fun onDownloadComplete(downloadRequestInfo: DownloadRequestInfo)

    /**
     * Called when a download request fails downloading.
     *
     * @param downloadRequestInfo The `DownloadRequestInfo` for which this callback was called.
     * @param error               Information about the error.
     */
    fun onDownloadError(downloadRequestInfo: DownloadRequestInfo, error: Error)

    /**
     * Called when all the download requests has finished downloading and the service is stopping,
     * before being destroyed.
     *
     * This method is called only once across the lifetime of the service. You should use this
     * callback method to clean up any resources you have created.
     * Override this method instead of [android.app.Service.onDestroy]
     */
    fun onStop()
}
