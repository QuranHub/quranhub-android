package app.quranhub.data.remote

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.remote.model.BookContent
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class BookDownloadManager(private val context: Context) {

    private var downloadManager: DownloadManager

    init {
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    fun downloadFile(book: BookContent): Long {
        val uri =
            Uri.parse(Constants.API_BASE_URL + book.path)
        getFileSize(Constants.API_BASE_URL + book.path)
        val file = File(
            Environment.getExternalStorageDirectory()
                .toString() + Constants.Directory.ROOT_PUBLIC
        )
        if (!file.exists()) {
            file.mkdir()
        }
        val request = DownloadManager.Request(uri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setTitle(book.name + " " + context.getString(R.string.download_file)) // Title for notification.
        request.setDestinationInExternalPublicDir(
            FILE_PATH,
            book.name + ".pdf"
        )
        return downloadManager.enqueue(request)
    }

    /**
     * Return the downloaded file size
     */
    @SuppressLint("StaticFieldLeak")
    private fun getFileSize(uri: String) {
        object : AsyncTask<Void?, Void?, Int>() {
            protected override fun doInBackground(vararg voids: Void?): Int {
                var fileSize = 0
                try {
                    val url = URL(uri)
                    val urlConnection = url.openConnection()
                    urlConnection.connect()
                    fileSize = urlConnection.contentLength
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return fileSize
            }
        }.execute()
    }

    fun cancelDownload(downloadId: Long) {
        downloadManager.remove(downloadId)
    }

    /**
     * get finished downloads
     */
    @SuppressLint("Range")
    fun queryOnFinishedDownloads(inProgressDownloadedIds: List<Long>): List<Int> {
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val imageDownloadQuery = DownloadManager.Query()
        val ids = LongArray(inProgressDownloadedIds.size)
        for (i in inProgressDownloadedIds.indices) {
            ids[i] = inProgressDownloadedIds[i]
        }
        imageDownloadQuery.setFilterById(*ids)
        var downloadStatus: Int
        val statusList: MutableList<Int> = ArrayList()
        val cursor = downloadManager.query(imageDownloadQuery)
        while (cursor.moveToNext()) {
            downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            statusList.add(downloadStatus)
            Log.d("tt7", " " + cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID)))
        }
        /*if(ids.length > 0 && statusList.size() >0)
            DownloadStatus(cursor, ids[0], statusList.get(0));*/cursor.close()
        return statusList
    }

    @SuppressLint("Range")
    fun queryOnFinishedDownloads(id: Long): Int {
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val imageDownloadQuery = DownloadManager.Query()
        imageDownloadQuery.setFilterById(id)
        var downloadStatus = DownloadManager.STATUS_SUCCESSFUL
        val cursor = downloadManager.query(imageDownloadQuery)
        if (cursor.moveToFirst()) {
            downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            Log.d("tt7", " " + cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID)))
        } else {
            downloadStatus = -1 // download is canceled
        }


        //DownloadStatus(cursor, 0, downloadStatus);
        cursor.close()
        return downloadStatus
    }

    private fun DownloadStatus(cursor: Cursor, DownloadId: Long, status: Int) {

        //column for download  status

        //column for reason code if the download failed or paused
        val reason = 1
        //get the download filename
        var statusText = ""
        var reasonText = ""
        when (status) {
            DownloadManager.STATUS_FAILED -> {
                statusText = "STATUS_FAILED"
                when (reason) {
                    DownloadManager.ERROR_CANNOT_RESUME -> reasonText = "ERROR_CANNOT_RESUME"
                    DownloadManager.ERROR_DEVICE_NOT_FOUND -> reasonText = "ERROR_DEVICE_NOT_FOUND"
                    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> reasonText =
                        "ERROR_FILE_ALREADY_EXISTS"

                    DownloadManager.ERROR_FILE_ERROR -> reasonText = "ERROR_FILE_ERROR"
                    DownloadManager.ERROR_HTTP_DATA_ERROR -> reasonText = "ERROR_HTTP_DATA_ERROR"
                    DownloadManager.ERROR_INSUFFICIENT_SPACE -> reasonText =
                        "ERROR_INSUFFICIENT_SPACE"

                    DownloadManager.ERROR_TOO_MANY_REDIRECTS -> reasonText =
                        "ERROR_TOO_MANY_REDIRECTS"

                    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> reasonText =
                        "ERROR_UNHANDLED_HTTP_CODE"

                    DownloadManager.ERROR_UNKNOWN -> reasonText = "ERROR_UNKNOWN"
                }
            }

            DownloadManager.STATUS_PAUSED -> {
                statusText = "STATUS_PAUSED"
                when (reason) {
                    DownloadManager.PAUSED_QUEUED_FOR_WIFI -> reasonText = "PAUSED_QUEUED_FOR_WIFI"
                    DownloadManager.PAUSED_UNKNOWN -> reasonText = "PAUSED_UNKNOWN"
                    DownloadManager.PAUSED_WAITING_FOR_NETWORK -> reasonText =
                        "PAUSED_WAITING_FOR_NETWORK"

                    DownloadManager.PAUSED_WAITING_TO_RETRY -> reasonText =
                        "PAUSED_WAITING_TO_RETRY"
                }
            }

            DownloadManager.STATUS_PENDING -> statusText = "STATUS_PENDING"
            DownloadManager.STATUS_RUNNING -> statusText = "STATUS_RUNNING"
            DownloadManager.STATUS_SUCCESSFUL -> statusText = "STATUS_SUCCESSFUL"
        }
        val toast = Toast.makeText(
            context,
            """
                 Music Download Status:
                 $statusText
                 $reasonText
                 """.trimIndent(),
            Toast.LENGTH_LONG
        )
        toast.setGravity(Gravity.TOP, 25, 400)
        toast.show()
    }

    companion object {
        private val TAG = BookDownloadManager::class.java.simpleName

        @JvmField
        val FILE_PATH = Constants.Directory.LIBRARY_PUBLIC
    }
}