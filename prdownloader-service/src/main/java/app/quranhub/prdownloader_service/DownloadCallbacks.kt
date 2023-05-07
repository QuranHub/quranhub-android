package app.quranhub.prdownloader_service;


import com.downloader.Error;
import com.downloader.Progress;

/**
 * Various callbacks for {@code PRDownloaderService}.
 * <p>
 * Do not call any of these methods directly.
 *
 * @author Abdallah Abdelazim <a href="mailto:abdallah.abdelazim@hotmail.com">abdallah.abdelazim@hotmail.com</a>
 * @see PRDownloaderService
 */
interface DownloadCallbacks {

    /**
     * Called when the service is being created, before any download request starts.
     * <p>This method is called only once across the lifetime of the service. You can use it for any
     * initialization.
     * Override this method instead of {@link android.app.Service#onCreate()}</p>
     */
    void onStart();

    /**
     * Called when a download request is being started or resumed.
     *
     * @param downloadRequestInfo The {@code DownloadRequestInfo} for which this callback was called.
     */
    void onDownloadStartOrResume(DownloadRequestInfo downloadRequestInfo);

    /**
     * Called when a download request is being paused.
     *
     * @param downloadRequestInfo The {@code DownloadRequestInfo} for which this callback was called.
     */
    void onDownloadPause(DownloadRequestInfo downloadRequestInfo);

    /**
     * Called when a download request is being cancelled.
     *
     * @param downloadRequestInfo The {@code DownloadRequestInfo} for which this callback was called.
     */
    void onDownloadCancel(DownloadRequestInfo downloadRequestInfo);

    /**
     * Called as a download request progress is updating.
     *
     * @param downloadRequestInfo The {@code DownloadRequestInfo} for which this callback was called.
     * @param progress            Information about the progress.
     */
    void onDownloadProgress(DownloadRequestInfo downloadRequestInfo, Progress progress);

    /**
     * Called when a download request has finished downloading successfully.
     *
     * @param downloadRequestInfo The {@code DownloadRequestInfo} for which this callback was called.
     */
    void onDownloadComplete(DownloadRequestInfo downloadRequestInfo);

    /**
     * Called when a download request fails downloading.
     *
     * @param downloadRequestInfo The {@code DownloadRequestInfo} for which this callback was called.
     * @param error               Information about the error.
     */
    void onDownloadError(DownloadRequestInfo downloadRequestInfo, Error error);

    /**
     * Called when all the download requests has finished downloading and the service is stopping,
     * before being destroyed.
     * <p>This method is called only once across the lifetime of the service. You should use this
     * callback method to clean up any resources you have created.
     * Override this method instead of {@link android.app.Service#onDestroy()}</p>
     */
    void onStop();

}
