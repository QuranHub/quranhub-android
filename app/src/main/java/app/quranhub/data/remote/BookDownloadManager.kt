package app.quranhub.data.remote;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.remote.model.BookContent;

public class BookDownloadManager {

    private static final String TAG = BookDownloadManager.class.getSimpleName();

    public static final String FILE_PATH = Constants.Directory.LIBRARY_PUBLIC;

    private DownloadManager downloadManager;

    @NonNull
    private Context context;

    public BookDownloadManager(@NonNull Context context) {
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        this.context = context;
    }

    public long downloadFile(BookContent book) {

        Uri uri = Uri.parse(Constants.API_BASE_URL + book.getPath());
        getFileSize(Constants.API_BASE_URL + book.getPath());
        File file = new File(Environment.getExternalStorageDirectory() + Constants.Directory.ROOT_PUBLIC);

        if (!file.exists()) {
            file.mkdir();
        }

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(book.getName() + " " + context.getString(R.string.download_file)); // Title for notification.
        request.setDestinationInExternalPublicDir(FILE_PATH, book.getName() + ".pdf");
        long downloadId = downloadManager.enqueue(request);
        return downloadId;
    }


    /**
     * Return the downloaded file size
     */
    @SuppressLint("StaticFieldLeak")
    private void getFileSize(String uri) {

        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                URL url = null;
                int file_size = 0;
                try {
                    url = new URL(uri);
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.connect();
                    file_size = urlConnection.getContentLength();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return file_size;
            }
        }.execute();

    }

    public void cancelDownload(long downloadId) {
        downloadManager.remove(downloadId);
    }

    /**
     * get finished downloads
     */
    public List<Integer> queryOnFinishedDownloads(List<Long> inProgressDownloadedIds) {
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query imageDownloadQuery = new DownloadManager.Query();
        long[] ids = new long[inProgressDownloadedIds.size()];
        for (int i = 0; i < inProgressDownloadedIds.size(); ++i) {
            ids[i] = inProgressDownloadedIds.get(i);
        }
        imageDownloadQuery.setFilterById(ids);
        int downloadStatus;
        List<Integer> statusList = new ArrayList<>();
        Cursor cursor = downloadManager.query(imageDownloadQuery);

        while (cursor.moveToNext()) {
            downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            statusList.add(downloadStatus);
            Log.d("tt7", " " + cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID)));
        }
        /*if(ids.length > 0 && statusList.size() >0)
            DownloadStatus(cursor, ids[0], statusList.get(0));*/
        cursor.close();
        return statusList;

    }

    public int queryOnFinishedDownloads(long id) {
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query imageDownloadQuery = new DownloadManager.Query();

        imageDownloadQuery.setFilterById(id);
        int downloadStatus = DownloadManager.STATUS_SUCCESSFUL;
        Cursor cursor = downloadManager.query(imageDownloadQuery);

        if (cursor.moveToFirst()) {
            downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            Log.d("tt7", " " + cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID)));
        } else {
            downloadStatus = -1; // download is canceled
        }


        //DownloadStatus(cursor, 0, downloadStatus);
        cursor.close();
        return downloadStatus;
    }

    private void DownloadStatus(Cursor cursor, long DownloadId, int status) {

        //column for download  status

        //column for reason code if the download failed or paused

        int reason = 1;
        //get the download filename

        String statusText = "";
        String reasonText = "";

        switch (status) {
            case DownloadManager.STATUS_FAILED:
                statusText = "STATUS_FAILED";
                switch (reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        break;
                }
                break;
            case DownloadManager.STATUS_PAUSED:
                statusText = "STATUS_PAUSED";
                switch (reason) {
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;
            case DownloadManager.STATUS_PENDING:
                statusText = "STATUS_PENDING";
                break;
            case DownloadManager.STATUS_RUNNING:
                statusText = "STATUS_RUNNING";
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                statusText = "STATUS_SUCCESSFUL";
                break;
        }


        Toast toast = Toast.makeText(context,
                "Music Download Status:" + "\n" + statusText + "\n" +
                        reasonText,
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 25, 400);
        toast.show();
    }
}
