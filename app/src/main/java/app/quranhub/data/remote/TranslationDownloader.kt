package app.quranhub.data.remote

import android.content.Context
import android.util.Log
import app.quranhub.data.Constants
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.util.NetworkUtil
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.downloader.Progress

class TranslationDownloader(
    val translationBook: TranslationBook,
    appContext: Context,
    private val callback: TranslationDownloadCallback?
) {
    private val appContext: Context

    private var downloadId = 0

    init {
        this.appContext = appContext.applicationContext
    }

    fun download() {
        // TODO refactor to use foreground service
        val downloadUrl = Constants.API_BASE_URL + translationBook.fileDownloadPath
        val dbPath = appContext.getDatabasePath(translationBook.databaseName)
        Log.d(TAG, "download: downloadUrl = $downloadUrl , dbPath = $dbPath")

        // Make sure we have a path to the file
        dbPath.parentFile.mkdirs()
        object : Thread() {
            override fun run() {
                translationBook.downloadStatus = NetworkUtil.STATUS_DOWNLOADING
                UserDatabase.getInstance(appContext).translationBookDao.insert(translationBook)
            }
        }.start()
        downloadId = PRDownloader.download(downloadUrl, dbPath.parent, dbPath.name)
            .build()
            .setOnStartOrResumeListener {
                Log.d(TAG, "setOnStartOrResumeListener: downloadId = $downloadId")
                callback?.onDownloadStarted()
            }
            .setOnCancelListener {
                Log.d(TAG, "onCancel: downloadId = $downloadId")
                object : Thread() {
                    override fun run() {
                        UserDatabase.getInstance(appContext).translationBookDao.delete(
                            translationBook
                        )
                    }
                }.start()
                callback?.onDownloadCancelled()
            }
            .setOnProgressListener { progress: Progress ->
                Log.d(
                    TAG, "onProgress: downloadId = " + downloadId +
                            " -> progress = " + progress.currentBytes + "/" + progress.totalBytes
                )

                // progress on four increments to optimize performance
                val progressRatio = progress.currentBytes.toDouble() / progress.totalBytes
                if (progressRatio > 0.9) {
                    Log.d(TAG, "progress : 100%")
                    updateProgressPercentage(100)
                } else if (progressRatio > 0.75 && progressRatio < 0.80) {
                    Log.d(TAG, "progress : 75%")
                    updateProgressPercentage(75)
                } else if (progressRatio > 0.50 && progressRatio < 0.55) {
                    Log.d(TAG, "progress : 50%")
                    updateProgressPercentage(50)
                } else if (progressRatio > 0.25 && progressRatio < 0.30) {
                    Log.d(TAG, "progress : 25%")
                    updateProgressPercentage(25)
                }
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    Log.d(TAG, "PRDownloader: downloadId = $downloadId ->  completed")
                    object : Thread() {
                        override fun run() {
                            translationBook.downloadStatus = NetworkUtil.STATUS_DOWNLOADED
                            UserDatabase.getInstance(appContext).translationBookDao.insert(
                                translationBook
                            )
                        }
                    }.start()
                    callback?.onDownloadFinished()
                }

                override fun onError(error: Error) {
                    Log.e(TAG, "PRDownloader: downloadId = $downloadId ->  error")
                    object : Thread() {
                        override fun run() {
                            UserDatabase.getInstance(appContext).translationBookDao.delete(
                                translationBook
                            )
                        }
                    }.start()
                    callback?.onDownloadFailed()
                }
            })
    }

    fun cancel() {
        PRDownloader.cancel(downloadId)
    }

    private fun updateProgressPercentage(downloadLevelPercentage: Int) {
        object : Thread() {
            override fun run() {
                translationBook.downloadLevelPercentage = downloadLevelPercentage
                UserDatabase.getInstance(appContext).translationBookDao.insert(translationBook)
            }
        }.start()
    }

    interface TranslationDownloadCallback {
        fun onDownloadStarted()
        fun onDownloadFinished()
        fun onDownloadCancelled()
        fun onDownloadFailed()
    }

    companion object {
        private val TAG = TranslationDownloader::class.java.simpleName
        fun cancelAll() {
            PRDownloader.cancelAll()
        }
    }
}