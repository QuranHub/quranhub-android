package app.quranhub.prdownloader_service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.Status;
import com.downloader.utils.Utils;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Base class for any download service.
 * Extend this class instead of {@link Service} & add your custom download logic by overriding methods
 * {@link PRDownloaderService#onStart()}, {@link DownloadCallbacks#onDownloadStartOrResume(DownloadRequestInfo)},
 * {@link DownloadCallbacks#onDownloadPause(DownloadRequestInfo)}, {@link DownloadCallbacks#onDownloadCancel(DownloadRequestInfo)},
 * {@link DownloadCallbacks#onDownloadProgress(DownloadRequestInfo, Progress)}, {@link DownloadCallbacks#onDownloadComplete(DownloadRequestInfo)},
 * {@link DownloadCallbacks#onDownloadError(DownloadRequestInfo, Error)} & {@link PRDownloaderService#onStop()}.
 * <p>The start intent should contain the download files URL paths as a String array intent
 * extra (use {@link PRDownloaderService#EXTRA_DOWNLOAD_REQUEST_INFOS}).
 * Also, the service supports passing the following actions as an intent action:
 * {@link PRDownloaderService#ACTION_CANCEL} or {@link PRDownloaderService#ACTION_CANCEL_ALL_DOWNLOADS}.
 * You <em>must</em> call {@link PRDownloaderService#init} first thing from
 * {@link PRDownloaderService#onStart()}.</p>
 *
 * @author Abdallah Abdelazim <a href="mailto:abdallah.abdelazim@hotmail.com">abdallah.abdelazim@hotmail.com</a>
 * TODO review JavaDoc documentation
 */
public abstract class PRDownloaderService extends Service implements DownloadCallbacks {

    private static final String TAG = PRDownloaderService.class.getSimpleName();

    public static final String ACTION_DOWNLOAD = "PRDownloaderService.ACTION_DOWNLOAD";
    public static final String ACTION_CANCEL = "PRDownloaderService.ACTION_CANCEL";
    // TODO feature addition: implement ACTION_PAUSE & ACTION_RESUME as well.
    public static final String EXTRA_DOWNLOAD_REQUEST_INFOS = "PRDownloaderService.EXTRA_DOWNLOAD_REQUEST_INFOS";
    public static final String ACTION_CANCEL_ALL_DOWNLOADS = "PRDownloaderService.ACTION_CANCEL_ALL_DOWNLOADS";

    private static final String NOTIFICATION_CHANNEL_ID = "PRDownloaderService.NOTIFICATION_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 1;

    private final Object downloadTag = new Object();

    private AtomicInteger downloadCount = new AtomicInteger(0);

    private boolean isInitialized = false;

    /**
     * Base for files download URLs.
     */
    @Nullable
    private String baseUrl;

    /**
     * The directory in which to put downloaded files if not specified in
     * {@link DownloadRequestInfo} instances.
     */
    @Nullable
    private String defaultDirPath;

    /**
     * Title for the download notification.
     */
    @Nullable
    private String notificationTitle;

    /**
     * Skip downloading the file if it already exists.
     */
    private boolean skipIfFileExists = true;

    /**
     * Initializes service parameters.
     * You <em>must</em> call this method first thing from {@link PRDownloaderService#onStart()}.
     *
     * @param baseUrl           Base for files download URLs. If there's non, you can pass
     *                          in {@code null}.
     * @param defaultDirPath    The directory in which to put downloaded files if not specified
     *                          in {@link DownloadRequestInfo} instances. If you don't want to
     *                          specify one, you can pass in {@code null}.
     * @param skipIfFileExists  Skip downloading the file if it already exists.
     * @param notificationTitle Title for the download notification. If you don't want to show a
     *                          notification title, you can pass in {@code null}.
     * @see #onStart()
     */
    protected void init(@Nullable String baseUrl, @Nullable String defaultDirPath, boolean skipIfFileExists,
                        @Nullable String notificationTitle) {
        this.baseUrl = baseUrl;
        this.defaultDirPath = defaultDirPath;
        this.skipIfFileExists = skipIfFileExists;
        this.notificationTitle = notificationTitle;

        setupForegroundServiceNotification(); // kickoff the foreground service notification

        isInitialized = true;
    }

    /**
     * Initializes service parameters.
     * You <em>must</em> call this method first thing from {@link PRDownloaderService#onStart()}.
     *
     * @param baseUrl           Base for files download URLs. If there's non, you can pass
     *                          in {@code null}.
     * @param defaultDirPath    The directory in which to put downloaded files if not specified
     *                          in {@link DownloadRequestInfo} instances. If you don't want to
     *                          specify one, you can pass in {@code null}.
     * @param notificationTitle Title for the download notification. If you don't want to show a
     *                          notification title, you can pass in {@code null}.
     * @see #onStart()
     */
    protected void init(@Nullable String baseUrl, @Nullable String defaultDirPath,
                        @Nullable String notificationTitle) {
        init(baseUrl, defaultDirPath, true, notificationTitle);
    }

    @CallSuper
    @Override
    public void onCreate() {
        super.onCreate();
        onStart();
    }

    @CallSuper
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isInitialized) {
            throw new RuntimeException("The service was not initialized. You must override" +
                    " onStart() & call PRDownloaderService#init method providing the required params.");
        }

        if (intent != null) {

            if (intent.getAction() == null
                    || intent.getAction().equals(ACTION_DOWNLOAD)) { // START DOWNLOAD

                // TODO implement a thread pool or something
                new Thread() {
                    @Override
                    public void run() {
                        DownloadRequestInfo[] downloadRequestInfos = provideDownloadRequestInfos(intent);

                        if (downloadRequestInfos != null) {
                            for (DownloadRequestInfo dInfo : downloadRequestInfos) {
                                downloadFile(dInfo);
                            }
                        }

                        if (downloadCount.get() == 0) stopSelf();  // handle if no download started
                    }
                }.start();

            } else if (intent.getAction() != null
                    && intent.getAction().equals(ACTION_CANCEL)) {  // CANCEL DOWNLOADS

                // TODO implement CANCEL DOWNLOADS

            } else if (intent.getAction() != null
                    && intent.getAction().equals(ACTION_CANCEL_ALL_DOWNLOADS)) { // CANCEL ALL DOWNLOADS

                cancelAllDownloads();

            } else {

                throw new RuntimeException("Unknown intent action!");

            }

        }

        return START_STICKY;  // TODO study & check back onStartCommand return values
    }

    /**
     * Prepare {@link DownloadRequestInfo} objects for download.
     * <p>
     * This method will be called from a background thread. You don't need to start a new one inside it
     *
     * @param startIntent The service start intent (as received in {@code onStartCommand}).
     * @return An array of {@link DownloadRequestInfo} objects to be downloaded.
     */
    @Nullable
    protected DownloadRequestInfo[] provideDownloadRequestInfos(@NonNull Intent startIntent) {
        Parcelable[] downloadRequestInfosParcelables = startIntent.getParcelableArrayExtra(
                EXTRA_DOWNLOAD_REQUEST_INFOS);
        Objects.requireNonNull(downloadRequestInfosParcelables);

        int n = downloadRequestInfosParcelables.length;
        DownloadRequestInfo[] downloadRequestInfos = new DownloadRequestInfo[n];
        for (int i = 0; i < n; i++) {
            downloadRequestInfos[i] = (DownloadRequestInfo) downloadRequestInfosParcelables[i];
        }

        return downloadRequestInfos;
    }

    /**
     * This method is called from a background thread.
     *
     * @param downloadRequestInfo
     */
    private void downloadFile(@Nullable DownloadRequestInfo downloadRequestInfo) {
        if (downloadRequestInfo == null) return;

        String url;
        if (downloadRequestInfo.isUrlRelative()) {
            Objects.requireNonNull(baseUrl);
            url = baseUrl + downloadRequestInfo.getUrl();
        } else {
            url = downloadRequestInfo.getUrl();
        }

        String dirPath;
        if (downloadRequestInfo.getDirPath() != null) {
            dirPath = downloadRequestInfo.getDirPath();
        } else {
            Objects.requireNonNull(defaultDirPath);
            dirPath = defaultDirPath;
            downloadRequestInfo.dirPath = dirPath;
        }

        String fileName;
        if (downloadRequestInfo.getFileName() != null) {
            fileName = downloadRequestInfo.getFileName();
        } else {
            fileName = downloadRequestInfo.getUrl().substring(
                    downloadRequestInfo.getUrl().lastIndexOf("/") + 1);
            downloadRequestInfo.fileName = fileName;
        }

        if (skipIfFileExists) {
            String filePath = dirPath + fileName;
            if (new File(filePath).exists()) {
                Log.i(TAG, "File '" + filePath + "' already exists. Skipping..");
                return;
            }
        }

        int downloadId = Utils.getUniqueId(url, dirPath, fileName);
        if (PRDownloader.getStatus(downloadId) != Status.UNKNOWN) {
            Log.w(TAG, "Duplicate download request skipped");
            return;
        }

        downloadCount.incrementAndGet();

        PRDownloader.download(url, dirPath, fileName)
                .setTag(downloadTag)
                .build()
                .setOnStartOrResumeListener(() -> onDownloadStartOrResume(downloadRequestInfo))
                .setOnPauseListener(() -> onDownloadPause(downloadRequestInfo))
                .setOnCancelListener(() -> {
                    onDownloadCancel(downloadRequestInfo);
                    checkIfFinishedAllDownloads();
                })
                .setOnProgressListener(progress -> onDownloadProgress(downloadRequestInfo, progress))
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        PRDownloaderService.this.onDownloadComplete(downloadRequestInfo);
                        checkIfFinishedAllDownloads();
                    }

                    @Override
                    public void onError(Error error) {
                        onDownloadError(downloadRequestInfo, error);
                        // TODO implement retry on failure
                        checkIfFinishedAllDownloads();
                    }
                });
        // TODO prepare for request cancelling, pause & resume
    }

    private void setupForegroundServiceNotification() {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this
                , NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(notificationTitle)
                .setContentText(getString(R.string.download_notification_content_text))
                .setSmallIcon(R.drawable.ic_download)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        // Add cancel action button (to cancel all the downloads)
        Intent cancelIntent = new Intent(this, this.getClass());
        cancelIntent.setAction(ACTION_CANCEL_ALL_DOWNLOADS);
        PendingIntent cancelPendingIntent;
        int cancelPendingIntentFlags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                PendingIntent.FLAG_IMMUTABLE : 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cancelPendingIntent = PendingIntent.getForegroundService(this, 0,
                    cancelIntent, cancelPendingIntentFlags);
        } else {
            // Pre-O behavior.
            cancelPendingIntent = PendingIntent.getService(this, 0,
                    cancelIntent, cancelPendingIntentFlags);
        }
        builder.addAction(R.drawable.ic_close, getString(R.string.download_notification_cancel_button_title),
                cancelPendingIntent);

        // display indeterminate progress bar
        builder.setProgress(0, 0, true);
        Notification notification = builder.build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.download_service_notification_channel_name);
            String description = getString(R.string.download_service_notification_channel_description);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name
                    , NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void checkIfFinishedAllDownloads() {
        if (downloadCount.decrementAndGet() == 0) {
            stopSelf();
        }
    }

    private void cancelAllDownloads() {
        PRDownloader.cancel(downloadTag);
    }

    @CallSuper
    @Override
    public void onDestroy() {
        if (downloadCount.get() > 0) cancelAllDownloads();
        onStop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

}
