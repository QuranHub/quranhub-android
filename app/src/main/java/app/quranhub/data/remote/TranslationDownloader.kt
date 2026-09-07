package app.quranhub.data.remote

import android.content.Context
import android.util.Log
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.util.NetworkUtil
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.Progress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TranslationDownloader(
    val translationBook: TranslationBook,
    appContext: Context,
    private val callback: TranslationDownloadCallback?
) {
    private val appContext: Context

    private var downloadId = 0
    private var lastReportedPercent = Int.MIN_VALUE

    // IO scope for the download bookkeeping DB writes (structured coroutines
    // replacing the former raw Threads)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        this.appContext = appContext.applicationContext
    }

    fun download() {
        // TODO refactor to use foreground service
        val downloadUrl = translationBook.fileDownloadPath
        val dbPath = appContext.getDatabasePath(translationBook.databaseName)
        Log.d(TAG, "download: downloadUrl = $downloadUrl , dbPath = $dbPath")

        // Make sure we have a path to the file
        dbPath.parentFile.mkdirs()
        scope.launch {
            translationBook.downloadStatus = NetworkUtil.STATUS_DOWNLOADING
            UserDatabase.getInstance(appContext).translationBookDao.insert(translationBook)
        }
        downloadId = PRDownloader.download(downloadUrl, dbPath.parent, dbPath.name)
            .build()
            .setOnStartOrResumeListener {
                Log.d(TAG, "setOnStartOrResumeListener: downloadId = $downloadId")
                callback?.onDownloadStarted(translationBook)
            }
            .setOnCancelListener {
                Log.d(TAG, "onCancel: downloadId = $downloadId")
                // NonCancellable: the cleanup delete must run even when this
                // downloader's scope is cancelled with it
                scope.launch {
                    withContext(NonCancellable) {
                        UserDatabase.getInstance(appContext).translationBookDao.delete(
                            translationBook
                        )
                    }
                }
                callback?.onDownloadCancelled(translationBook)
            }
            .setOnProgressListener { progress: Progress ->
                val percent = if (progress.totalBytes <= 0L) {
                    UNKNOWN_PERCENT
                } else {
                    ((progress.currentBytes * 100) / progress.totalBytes)
                        .toInt()
                        .coerceIn(0, 100)
                }
                if (percent != lastReportedPercent) {
                    lastReportedPercent = percent
                    Log.d(
                        TAG,
                        "onProgress: downloadId = $downloadId -> $percent% " +
                            "(${progress.currentBytes}/${progress.totalBytes})"
                    )
                    updateProgressPercentage(percent)
                }
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Log.d(TAG, "PRDownloader: downloadId = $downloadId ->  completed")
                    scope.launch {
                        translationBook.downloadStatus = NetworkUtil.STATUS_DOWNLOADED
                        UserDatabase.getInstance(appContext).translationBookDao.insert(
                            translationBook
                        )
                    }
                    callback?.onDownloadFinished(translationBook)
                }

                override fun onError(error: Error) {
                    Log.e(TAG, "PRDownloader: downloadId = $downloadId ->  error")
                    scope.launch {
                        withContext(NonCancellable) {
                            UserDatabase.getInstance(appContext).translationBookDao.delete(
                                translationBook
                            )
                        }
                    }
                    callback?.onDownloadFailed(translationBook)
                }
            })
    }

    /**
     * Cancels the PRDownloader request and this downloader's coroutine scope.
     * The cleanup DB deletes (cancel/error) still run via [NonCancellable].
     */
    fun cancel() {
        PRDownloader.cancel(downloadId)
        scope.cancel()
    }

    private fun updateProgressPercentage(downloadLevelPercentage: Int) {
        translationBook.downloadLevelPercentage = downloadLevelPercentage
        callback?.onDownloadProgress(translationBook, downloadLevelPercentage)
        if (downloadLevelPercentage >= 0 && downloadLevelPercentage % 25 == 0) {
            scope.launch {
                UserDatabase.getInstance(appContext).translationBookDao.insert(translationBook)
            }
        }
    }

    interface TranslationDownloadCallback {
        fun onDownloadStarted(book: TranslationBook)
        fun onDownloadProgress(book: TranslationBook, percent: Int)
        fun onDownloadFinished(book: TranslationBook)
        fun onDownloadCancelled(book: TranslationBook)
        fun onDownloadFailed(book: TranslationBook)
    }

    companion object {
        private val TAG = TranslationDownloader::class.java.simpleName
        const val UNKNOWN_PERCENT = -1
        fun cancelAll() {
            PRDownloader.cancelAll()
        }
    }
}
