package app.quranhub.prdownloader_service

import android.app.ForegroundServiceStartNotAllowedException
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.Progress
import com.downloader.Status
import com.downloader.utils.Utils
import java.io.File
import java.util.Objects
import java.util.concurrent.atomic.AtomicInteger

/**
 * Base class for any download service.
 * Extend this class instead of [Service] & add your custom download logic by overriding methods
 * [PRDownloaderService.onStart], [DownloadCallbacks.onDownloadStartOrResume],
 * [DownloadCallbacks.onDownloadPause], [DownloadCallbacks.onDownloadCancel],
 * [DownloadCallbacks.onDownloadProgress], [DownloadCallbacks.onDownloadComplete],
 * [DownloadCallbacks.onDownloadError] & [PRDownloaderService.onStop].
 *
 * The start intent should contain the download files URL paths as a String array intent
 * extra (use [PRDownloaderService.EXTRA_DOWNLOAD_REQUEST_INFOS]).
 * Also, the service supports passing the following actions as an intent action:
 * [PRDownloaderService.ACTION_CANCEL] or [PRDownloaderService.ACTION_CANCEL_ALL_DOWNLOADS].
 * You *must* call [PRDownloaderService.init] first thing from
 * [PRDownloaderService.onStart].
 *
 * @author Abdallah Abdelazim [abdallah.abdelazim@hotmail.com](mailto:abdallah.abdelazim@hotmail.com)
 * TODO review JavaDoc documentation
 */
abstract class PRDownloaderService : Service(), DownloadCallbacks {

    private val downloadTag = Any()
    private val downloadCount = AtomicInteger(0)
    private var isInitialized = false

    /**
     * Base for files download URLs.
     */
    private var baseUrl: String? = null

    /**
     * The directory in which to put downloaded files if not specified in
     * [DownloadRequestInfo] instances.
     */
    private var defaultDirPath: String? = null

    /**
     * Title for the download notification.
     */
    private var notificationTitle: String? = null

    /**
     * Skip downloading the file if it already exists.
     */
    private var skipIfFileExists = true

    /**
     * Initializes service parameters.
     * You *must* call this method first thing from [PRDownloaderService.onStart].
     *
     * @param baseUrl           Base for files download URLs. If there's non, you can pass
     * in `null`.
     * @param defaultDirPath    The directory in which to put downloaded files if not specified
     * in [DownloadRequestInfo] instances. If you don't want to
     * specify one, you can pass in `null`.
     * @param skipIfFileExists  Skip downloading the file if it already exists.
     * @param notificationTitle Title for the download notification. If you don't want to show a
     * notification title, you can pass in `null`.
     * @see .onStart
     */
    protected fun init(
        baseUrl: String?, defaultDirPath: String?, skipIfFileExists: Boolean,
        notificationTitle: String?
    ) {
        this.baseUrl = baseUrl
        this.defaultDirPath = defaultDirPath
        this.skipIfFileExists = skipIfFileExists
        this.notificationTitle = notificationTitle
        setupForegroundServiceNotification() // kickoff the foreground service notification
        isInitialized = true
    }

    /**
     * Initializes service parameters.
     * You *must* call this method first thing from [PRDownloaderService.onStart].
     *
     * @param baseUrl           Base for files download URLs. If there's non, you can pass
     * in `null`.
     * @param defaultDirPath    The directory in which to put downloaded files if not specified
     * in [DownloadRequestInfo] instances. If you don't want to
     * specify one, you can pass in `null`.
     * @param notificationTitle Title for the download notification. If you don't want to show a
     * notification title, you can pass in `null`.
     * @see .onStart
     */
    protected fun init(
        baseUrl: String?, defaultDirPath: String?,
        notificationTitle: String?
    ) {
        init(baseUrl, defaultDirPath, true, notificationTitle)
    }

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        onStart()
    }

    @CallSuper
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_STICKY

        if (!isInitialized) {
            throw RuntimeException(
                "The service was not initialized. You must override" +
                        " onStart() & call PRDownloaderService#init method providing the required params."
            )
        }
        if (intent.action == null || intent.action == ACTION_DOWNLOAD) { // START DOWNLOAD

            // TODO implement a thread pool or something
            object : Thread() {
                override fun run() {
                    val downloadRequestInfos = provideDownloadRequestInfos(intent)
                    for (dInfo in downloadRequestInfos) {
                        downloadFile(dInfo)
                    }
                    if (downloadCount.get() == 0) stopSelf() // handle if no download started
                }
            }.start()
        } else if (intent.action != null && intent.action == ACTION_CANCEL) {  // CANCEL DOWNLOADS

            // TODO implement CANCEL DOWNLOADS
        } else if (intent.action != null && intent.action == ACTION_CANCEL_ALL_DOWNLOADS) { // CANCEL ALL DOWNLOADS
            cancelAllDownloads()
        } else {
            throw RuntimeException("Unknown intent action!")
        }
        return START_STICKY // TODO study & check back onStartCommand return values
    }

    /**
     * Prepare [DownloadRequestInfo] objects for download.
     *
     *
     * This method will be called from a background thread. You don't need to start a new one inside it
     *
     * @param startIntent The service start intent (as received in `onStartCommand`).
     * @return An array of [DownloadRequestInfo] objects to be downloaded.
     */
    protected open fun provideDownloadRequestInfos(startIntent: Intent): Array<DownloadRequestInfo> {
        val downloadRequestInfosParcelables = startIntent.getParcelableArrayExtra(
            EXTRA_DOWNLOAD_REQUEST_INFOS
        )
        Objects.requireNonNull(downloadRequestInfosParcelables)
        val n = downloadRequestInfosParcelables!!.size
        val downloadRequestInfos = mutableListOf<DownloadRequestInfo>()
        for (i in 0 until n) {
            downloadRequestInfos += downloadRequestInfosParcelables[i] as DownloadRequestInfo
        }
        return downloadRequestInfos.toTypedArray()
    }

    /**
     * This method is called from a background thread.
     *
     * @param downloadRequestInfo
     */
    private fun downloadFile(downloadRequestInfo: DownloadRequestInfo?) {
        if (downloadRequestInfo == null) return
        val url: String = if (downloadRequestInfo.isUrlRelative) {
            Objects.requireNonNull(baseUrl)
            baseUrl + downloadRequestInfo.url
        } else {
            downloadRequestInfo.url
        }
        val dirPath: String?
        if (downloadRequestInfo.dirPath != null) {
            dirPath = downloadRequestInfo.dirPath
        } else {
            Objects.requireNonNull(defaultDirPath)
            dirPath = defaultDirPath
            downloadRequestInfo.dirPath = dirPath
        }
        val fileName: String?
        if (downloadRequestInfo.fileName != null) {
            fileName = downloadRequestInfo.fileName
        } else {
            fileName = downloadRequestInfo.url.substring(
                downloadRequestInfo.url.lastIndexOf("/") + 1
            )
            downloadRequestInfo.fileName = fileName
        }
        if (skipIfFileExists) {
            val filePath = dirPath + fileName
            if (File(filePath).exists()) {
                Log.i(TAG, "File '$filePath' already exists. Skipping..")
                return
            }
        }
        val downloadId = Utils.getUniqueId(url, dirPath, fileName)
        if (PRDownloader.getStatus(downloadId) != Status.UNKNOWN) {
            Log.w(TAG, "Duplicate download request skipped")
            return
        }
        downloadCount.incrementAndGet()
        PRDownloader.download(url, dirPath, fileName)
            .setTag(downloadTag)
            .build()
            .setOnStartOrResumeListener { onDownloadStartOrResume(downloadRequestInfo) }
            .setOnPauseListener { onDownloadPause(downloadRequestInfo) }
            .setOnCancelListener {
                onDownloadCancel(downloadRequestInfo)
                checkIfFinishedAllDownloads()
            }
            .setOnProgressListener { progress: Progress? ->
                onDownloadProgress(
                    downloadRequestInfo,
                    progress!!
                )
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    this@PRDownloaderService.onDownloadComplete(downloadRequestInfo)
                    checkIfFinishedAllDownloads()
                }

                override fun onError(error: Error) {
                    onDownloadError(downloadRequestInfo, error)
                    // TODO implement retry on failure
                    checkIfFinishedAllDownloads()
                }
            })
        // TODO prepare for request cancelling, pause & resume
    }

    private fun setupForegroundServiceNotification() {
        createNotificationChannel()
        val builder = NotificationCompat.Builder(
            this, NOTIFICATION_CHANNEL_ID
        )
        builder.setContentTitle(notificationTitle)
            .setContentText(getString(R.string.download_notification_content_text))
            .setSmallIcon(R.drawable.ic_download).priority =
            NotificationCompat.PRIORITY_LOW

        // Add cancel action button (to cancel all the downloads)
        val cancelIntent = Intent(this, this.javaClass)
        cancelIntent.action = ACTION_CANCEL_ALL_DOWNLOADS
        val cancelPendingIntent: PendingIntent
        val cancelPendingIntentFlags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        cancelPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                this, 0,
                cancelIntent, cancelPendingIntentFlags
            )
        } else {
            // Pre-O behavior.
            PendingIntent.getService(
                this, 0,
                cancelIntent, cancelPendingIntentFlags
            )
        }
        builder.addAction(
            R.drawable.ic_close, getString(R.string.download_notification_cancel_button_title),
            cancelPendingIntent
        )

        // display indeterminate progress bar
        builder.setProgress(0, 0, true)
        val notification = builder.build()
        promoteToForegroundService(notification)
    }

    private fun promoteToForegroundService(notification: Notification) {
        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                } else {
                    0
                },
            )
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && e is ForegroundServiceStartNotAllowedException
            ) {
                // App not in a valid state to start foreground service
                // (e.g. started from bg)
                Log.e(TAG, "Foreground service start not allowed")
            }
            Toast.makeText(
                this, R.string.msg_download_service_failed_to_start,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.download_service_notification_channel_name)
            val description = getString(R.string.download_service_notification_channel_description)
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW
            )
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkIfFinishedAllDownloads() {
        if (downloadCount.decrementAndGet() == 0) {
            stopSelf()
        }
    }

    private fun cancelAllDownloads() {
        PRDownloader.cancel(downloadTag)
    }

    @CallSuper
    override fun onDestroy() {
        if (downloadCount.get() > 0) cancelAllDownloads()
        onStop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    companion object {
        private val TAG = PRDownloaderService::class.java.simpleName
        const val ACTION_DOWNLOAD = "PRDownloaderService.ACTION_DOWNLOAD"
        const val ACTION_CANCEL = "PRDownloaderService.ACTION_CANCEL"

        // TODO feature addition: implement ACTION_PAUSE & ACTION_RESUME as well.
        const val EXTRA_DOWNLOAD_REQUEST_INFOS = "PRDownloaderService.EXTRA_DOWNLOAD_REQUEST_INFOS"
        const val ACTION_CANCEL_ALL_DOWNLOADS = "PRDownloaderService.ACTION_CANCEL_ALL_DOWNLOADS"
        private const val NOTIFICATION_CHANNEL_ID = "PRDownloaderService.NOTIFICATION_CHANNEL_ID"
        private const val NOTIFICATION_ID = 1
    }
}