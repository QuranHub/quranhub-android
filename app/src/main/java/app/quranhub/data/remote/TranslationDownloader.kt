package app.quranhub.data.remote;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;

import java.io.File;

import app.quranhub.data.Constants;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.entity.TranslationBook;
import app.quranhub.util.NetworkUtil;

public class TranslationDownloader {

    private static final String TAG = TranslationDownloader.class.getSimpleName();

    @NonNull
    private Context appContext;
    @NonNull
    private TranslationBook translationBook;
    @Nullable
    private TranslationDownloadCallback callback;

    private int downloadId;


    public TranslationDownloader(@NonNull TranslationBook translationBook, @NonNull Context appContext
            , @Nullable TranslationDownloadCallback callback) {
        this.translationBook = translationBook;
        this.appContext = appContext.getApplicationContext();
        this.callback = callback;
    }

    public void download() {
        // TODO refactor to use foreground service

        String downloadUrl = Constants.API_BASE_URL + translationBook.getFileDownloadPath();
        File dbPath = appContext.getDatabasePath(translationBook.getDatabaseName());

        Log.d(TAG, "download: downloadUrl = " + downloadUrl + " , dbPath = " + dbPath);

        // Make sure we have a path to the file
        dbPath.getParentFile().mkdirs();

        new Thread() {
            @Override
            public void run() {
                translationBook.setDownloadStatus(NetworkUtil.STATUS_DOWNLOADING);
                UserDatabase.getInstance(appContext).getTranslationBookDao().insert(translationBook);
            }
        }.start();

        downloadId = PRDownloader.download(downloadUrl, dbPath.getParent(), dbPath.getName())
                .build()
                .setOnStartOrResumeListener(() -> {
                    Log.d(TAG, "setOnStartOrResumeListener: downloadId = " + downloadId);

                    if (callback != null) {
                        callback.onDownloadStarted();
                    }
                })
                .setOnCancelListener(() -> {
                    Log.d(TAG, "onCancel: downloadId = " + downloadId);

                    new Thread() {
                        @Override
                        public void run() {
                            UserDatabase.getInstance(appContext).getTranslationBookDao().delete(translationBook);
                        }
                    }.start();

                    if (callback != null) {
                        callback.onDownloadCancelled();
                    }
                })
                .setOnProgressListener(progress -> {
                    Log.d(TAG, "onProgress: downloadId = " + downloadId +
                            " -> progress = " + progress.currentBytes + "/" + progress.totalBytes);

                    // progress on four increments to optimize performance
                    double progressRatio = (double) progress.currentBytes / progress.totalBytes;
                    if (progressRatio > 0.9d) {
                        Log.d(TAG, "progress : 100%");
                        updateProgressPercentage(100);
                    } else if (progressRatio > 0.75d && progressRatio < 0.80d) {
                        Log.d(TAG, "progress : 75%");
                        updateProgressPercentage(75);
                    } else if (progressRatio > 0.50d && progressRatio < 0.55d) {
                        Log.d(TAG, "progress : 50%");
                        updateProgressPercentage(50);
                    } else if (progressRatio > 0.25d && progressRatio < 0.30d) {
                        Log.d(TAG, "progress : 25%");
                        updateProgressPercentage(25);
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        Log.d(TAG, "PRDownloader: downloadId = " + downloadId + " ->  completed");

                        new Thread() {
                            @Override
                            public void run() {
                                translationBook.setDownloadStatus(NetworkUtil.STATUS_DOWNLOADED);
                                UserDatabase.getInstance(appContext).getTranslationBookDao().insert(translationBook);
                            }
                        }.start();

                        if (callback != null) {
                            callback.onDownloadFinished();
                        }
                    }

                    @Override
                    public void onError(Error error) {
                        Log.e(TAG, "PRDownloader: downloadId = " + downloadId + " ->  error");

                        new Thread() {
                            @Override
                            public void run() {
                                UserDatabase.getInstance(appContext).getTranslationBookDao().delete(translationBook);
                            }
                        }.start();

                        if (callback != null) {
                            callback.onDownloadFailed();
                        }
                    }
                });
    }

    public void cancel() {
        PRDownloader.cancel(downloadId);
    }

    public static void cancelAll() {
        PRDownloader.cancelAll();
    }

    private void updateProgressPercentage(int downloadLevelPercentage) {
        new Thread() {
            @Override
            public void run() {
                translationBook.setDownloadLevelPercentage(downloadLevelPercentage);
                UserDatabase.getInstance(appContext).getTranslationBookDao().insert(translationBook);
            }
        }.start();
    }

    @NonNull
    public TranslationBook getTranslationBook() {
        return translationBook;
    }


    public interface TranslationDownloadCallback {
        void onDownloadStarted();

        void onDownloadFinished();

        void onDownloadCancelled();

        void onDownloadFailed();
    }
}
