package app.quranhub.data.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.downloader.Error;
import com.downloader.Progress;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.dao.AyaDao;
import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.entity.Aya;
import app.quranhub.data.local.entity.QuranAudio;
import app.quranhub.prdownloader_service.DownloadRequestInfo;
import app.quranhub.prdownloader_service.PRDownloaderService;
import app.quranhub.util.LocaleUtils;
import app.quranhub.util.QuranAudioDownloadUtils;
import app.quranhub.util.QuranAudioFileUtils;

/**
 * {@code PRDownloaderService} for Quran audio files.
 * <p>
 * To start this service use one of {@link QuranAudioDownloaderService#downloadSura(Context, int, String, int)}
 * , {@link QuranAudioDownloaderService#downloadQuran(Context, int, String)} or
 * {@link QuranAudioDownloaderService#downloadAyaRange(Context, int, String, int, int)}.
 * </p>
 * <p>
 * You can subscribe to {@link DownloadFinishEvent} with {@code EventBus} to get notified when all
 * the downloads finish, either successfully or unsuccessfully, and this service stops.
 * </p>
 *
 * @author Abdallah Abdelazim
 * @see PRDownloaderService
 */
public class QuranAudioDownloaderService extends PRDownloaderService {

    private static final String TAG = QuranAudioDownloaderService.class.getSimpleName();

    private static final String EXTRA_START_AYA_ID = "EXTRA_START_AYA_ID";
    private static final String EXTRA_END_AYA_ID = "EXTRA_END_AYA_ID";
    private static final String EXTRA_RECITATION_ID = "EXTRA_RECITATION_ID";
    private static final String EXTRA_RECITER_ID = "EXTRA_RECITER_ID";

    private static final String DRI_EXTRA_INFO_AYA_ID = "DRI_EXTRA_INFO_AYA_ID";
    private static final String DRI_EXTRA_INFO_RECITATION_ID = "DRI_EXTRA_INFO_RECITATION_ID";
    private static final String DRI_EXTRA_INFO_RECITER_ID = "DRI_EXTRA_INFO_RECITER_ID";

    public static void downloadSura(@NonNull Context context, int recitationId, String reciterId,
                                    int suraId) {

        new AsyncTask<Void, Void, Pair<Integer, Integer>>() {

            @Override
            protected Pair<Integer, Integer> doInBackground(Void... voids) {
                AyaDao ayaDao = MushafDatabase.getInstance(context).getAyaDao();

                int startAyaId = ayaDao.getFirstAyaInSura(suraId).getId();
                int endAyaId = ayaDao.getLastAyaInSura(suraId).getId();

                return new Pair<>(startAyaId, endAyaId);
            }

            @Override
            protected void onPostExecute(Pair<Integer, Integer> ayaIdPair) {
                downloadAyaRange(context, recitationId, reciterId, ayaIdPair.first, ayaIdPair.second);
            }
        }.execute();

    }

    public static void downloadQuran(@NonNull Context context, int recitationId, String reciterId) {
        downloadAyaRange(context, recitationId, reciterId, 1, 6236);
    }

    /**
     * Downloads Aya audio for the given recitation & reciter from {@code startAyaId}
     * to {@code endAyaId} inclusive.
     */
    public static void downloadAyaRange(@NonNull Context context, int recitationId, String reciterId,
                                        int startAyaId, int endAyaId) {
        if (reciterId != null) {
            Intent intent = new Intent(context, QuranAudioDownloaderService.class);
            intent.setAction(ACTION_DOWNLOAD);
            intent.putExtra(EXTRA_START_AYA_ID, startAyaId);
            intent.putExtra(EXTRA_END_AYA_ID, endAyaId);
            intent.putExtra(EXTRA_RECITATION_ID, recitationId);
            intent.putExtra(EXTRA_RECITER_ID, reciterId);
            ContextCompat.startForegroundService(context, intent);
        } else {
            Log.w(TAG, "'reciterId' arg is null. Service will not be started.");
            Toast.makeText(context, R.string.msg_download_no_reciter, Toast.LENGTH_SHORT).show();
        }
    }

    public static void cancelAllDownloads(@NonNull Context context) {
        Intent intent = new Intent(context, QuranAudioDownloaderService.class);
        intent.setAction(ACTION_CANCEL_ALL_DOWNLOADS);
        ContextCompat.startForegroundService(context, intent);
    }

    @Nullable
    @Override
    protected DownloadRequestInfo[] provideDownloadRequestInfos(@NonNull Intent startIntent) {
        int startAyaId = startIntent.getIntExtra(EXTRA_START_AYA_ID, -1);
        int endAyaId = startIntent.getIntExtra(EXTRA_END_AYA_ID, -1);
        int recitationId = startIntent.getIntExtra(EXTRA_RECITATION_ID, -1);
        String reciterId = startIntent.getStringExtra(EXTRA_RECITER_ID);

        if (startAyaId == -1 || endAyaId == -1 || recitationId == -1 || reciterId == null) {
            throw new RuntimeException("MISSING INTENT EXTRAS: You must put EXTRA_START_AYA_ID," +
                    " EXTRA_END_AYA_ID, EXTRA_RECITATION_ID & EXTRA_RECITER_ID intent extras to the" +
                    " start intent of QuranAudioDownloaderService.");
        }

        AyaDao ayaDao = MushafDatabase.getInstance(this).getAyaDao();
        List<DownloadRequestInfo> downloadRequestInfos = new ArrayList<>();

        for (int ayaId = startAyaId; ayaId <= endAyaId; ayaId++) {
            Aya aya = ayaDao.findAyaById(ayaId);
            String urlPath = QuranAudioDownloadUtils.getDownloadUrlPath(recitationId
                    , reciterId, aya.getSura(), aya.getSuraAya());
            String dirPath = QuranAudioFileUtils.getLocalDirPath(this,
                    recitationId, reciterId);
            Bundle extraInfo = new Bundle();
            extraInfo.putInt(DRI_EXTRA_INFO_AYA_ID, ayaId);
            extraInfo.putInt(DRI_EXTRA_INFO_RECITATION_ID, recitationId);
            extraInfo.putString(DRI_EXTRA_INFO_RECITER_ID, reciterId);
            downloadRequestInfos.add(
                    new DownloadRequestInfo.Builder(urlPath, true)
                            .setDirPath(dirPath)
                            .setExtraInfo(extraInfo)
                            .build());
        }

        return downloadRequestInfos.toArray(new DownloadRequestInfo[0]);
    }

    @Override
    public void onCreate() {
        LocaleUtils.initAppLanguage(this);
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleUtils.initAppLanguage(newBase));
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");

        init(Constants.STATIC_FILES_BASE_URL, null, getString(R.string.download_notification_title_quran_audio));
    }

    @Override
    public void onDownloadStartOrResume(DownloadRequestInfo downloadRequestInfo) {
        Log.d(TAG, "onDownloadStartOrResume :: downloadRequestInfo=" + downloadRequestInfo);

    }

    @Override
    public void onDownloadPause(DownloadRequestInfo downloadRequestInfo) {
        Log.d(TAG, "onDownloadPause :: downloadRequestInfo=" + downloadRequestInfo);

    }

    @Override
    public void onDownloadCancel(DownloadRequestInfo downloadRequestInfo) {
        Log.d(TAG, "onDownloadCancel :: downloadRequestInfo=" + downloadRequestInfo);

    }

    @Override
    public void onDownloadProgress(DownloadRequestInfo downloadRequestInfo, Progress progress) {

    }

    @Override
    public void onDownloadComplete(DownloadRequestInfo downloadRequestInfo) {
        Log.d(TAG, "onDownloadComplete :: downloadRequestInfo=" + downloadRequestInfo);

        new Thread() {
            @Override
            public void run() {
                int recitationId = downloadRequestInfo.getExtraInfo().getInt(DRI_EXTRA_INFO_RECITATION_ID);
                String reciterId = downloadRequestInfo.getExtraInfo().getString(DRI_EXTRA_INFO_RECITER_ID);
                int ayaId = downloadRequestInfo.getExtraInfo().getInt(DRI_EXTRA_INFO_AYA_ID);

                MushafDatabase mushafDatabase = MushafDatabase.getInstance(QuranAudioDownloaderService.this);
                UserDatabase userDatabase = UserDatabase.getInstance(QuranAudioDownloaderService.this);

                Aya aya = mushafDatabase.getAyaDao().findAyaById(ayaId);
                String filePath = QuranAudioFileUtils.getLocalRelativeDirPath(recitationId, reciterId)
                        + downloadRequestInfo.getFileName();
                int sheikhRecitationId = userDatabase.getReciterRecitationDao()
                        .getSheikhRecitationId(recitationId, reciterId);
                QuranAudio quranAudio = new QuranAudio(aya.getPage(), aya.getSura(), aya.getSuraAya(),
                        ayaId, filePath, sheikhRecitationId);

                userDatabase.getQuranAudioDao().insert(quranAudio);
            }
        }.start();

    }

    @Override
    public void onDownloadError(DownloadRequestInfo downloadRequestInfo, Error error) {
        Log.e(TAG, "onDownloadError :: downloadRequestInfo=" + downloadRequestInfo +
                " , error code=" + error.getResponseCode());

    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");

//        Toast.makeText(this, R.string.toast_download_quran_audio_finished, Toast.LENGTH_SHORT).show();
        EventBus.getDefault().post(new DownloadFinishEvent());
    }


    /**
     * {@code EventBus} event that gets posted when all the downloads finish, either successfully or
     * unsuccessfully, and {@link QuranAudioDownloaderService} stops.
     */
    public static class DownloadFinishEvent {

    }
}
