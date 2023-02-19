package app.quranhub.ui.mushaf.audio_manager;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.ui.base.BaseService;
import app.quranhub.ui.main.MainActivity;
import app.quranhub.ui.mushaf.model.AyaIdInfo;
import app.quranhub.ui.mushaf.model.RepeatModel;
import app.quranhub.ui.mushaf.model.SuraVersesNumber;
import app.quranhub.util.LocaleUtils;
import app.quranhub.util.SharedPrefsUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AyaAudioService extends BaseService
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static final String NOTIFICATION_CHANNEL_ID = "AyaAudioService.NOTIFICATION_CHANNEL_ID";
    private static final int NOTIFICATION_ID = 2;

    public static final String ACTION_PLAY = "PLAY";
    public static final String ACTION_RESUME = "RESUME";
    public static final String ACTION_PAUSE = "PAUSE";
    public static final String ACTION_STOP = "STOP";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREVIOUS = "PREVIOUS";

    private static final int REQUEST_CODE_MAIN = 0;
    private static final int REQUEST_CODE_PREVIOUS = 1;
    private static final int REQUEST_CODE_PAUSE = 2;
    private static final int REQUEST_CODE_NEXT = 3;
    private static final int REQUEST_CODE_STOP = 4;
    private static final int REQUEST_CODE_RESUME = 5;

    private static final int AYA_REPEAT_CASE = 1;
    private static final int GROUP_REPEAT_CASE = 2;
    private static final int NEXT_AYA_CASE = 4;


    public static final String AYA_ID_KEY = "AYA_ID_KEY";
    public static final String FROM_NOTIFICATION = "FROM_NOTIFICATION";
    public static final String SURA_VERSES_KEY = "SURA_VERSES_KEY";
    public static final String SERVICE_RUNNING = "SERVICE_RUNNING";
    public static final String AUDIO_PLAYING = "AUDIO_PLAYING";
    private static final String TAG = "AyaAudioService.service";

    private Runnable playAudioDelayRunnable;
    private Handler playAudioHandler;
    private int currentAyaRepeatNumber = 1, currentGroupRepeatNumber = 1;
    private String currentAudioPath;
    private boolean fromNotification;
    private MediaPlayer mediaPlayer;
    private Bitmap notificationIcon;
    private PendingIntent notificationIntent, resumeIntent, nextIntent, prevIntent, stopIntent, pauseIntent;
    private UserDatabase userDatabase;
    private int currentAyaId;
    private ArrayList<SuraVersesNumber> suraVersesNumberArrayList;
    private ArrayList<AyaIdInfo> ayaIdInfoArrayList;
    private String[] suras;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationCompat.Builder pausedNotificationBuilder;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotification();
        initPlayer();
        userDatabase = UserDatabase.getInstance(this);
    }

    private void initPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    private void createNotification() {
        createNotificationChannel();
        setNotificationIcon();
        setActionsIntent();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationBuilder == null) {
            notificationBuilder = new NotificationCompat.Builder(this
                    , NOTIFICATION_CHANNEL_ID);
            notificationBuilder.setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.audio_playing))
                    .setContentTitle(getString(R.string.sura))
                    .setLargeIcon(notificationIcon)
                    .setSmallIcon(R.drawable.play_ayha_action_white_ic)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentIntent(notificationIntent)
                    .addAction(R.drawable.player_fast_rewind_white_ic, getString(R.string.prev), prevIntent)
                    .addAction(R.drawable.ic_pause, getString(R.string.pause), pauseIntent)
                    .addAction(R.drawable.player_fast_forward_white_ic, getString(R.string.next), nextIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2));
        }
        if (pausedNotificationBuilder == null) {
            pausedNotificationBuilder = new NotificationCompat.Builder(this
                    , NOTIFICATION_CHANNEL_ID);
            pausedNotificationBuilder.setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.audio_playing))
                    .setContentTitle(getString(R.string.sura))
                    .setLargeIcon(notificationIcon)
                    .setSmallIcon(R.drawable.play_ayha_action_white_ic)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentIntent(notificationIntent)
                    .addAction(R.drawable.ic_new_close, getString(R.string.stop), stopIntent)
                    .addAction(R.drawable.player_play_white_ic, getString(R.string.resume), resumeIntent)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1));
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.audio_playing);
            String description = getString(R.string.audio_playing_desc);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name
                    , NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateNotificationState(boolean isPausd) {
        SharedPrefsUtils.saveBoolean(this, AUDIO_PLAYING, !isPausd);
        if (isPausd) {
            notificationManager.notify(NOTIFICATION_ID, pausedNotificationBuilder.build());
        } else {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private void updateNotificationContent(boolean isPausd, AyaIdInfo ayaIdInfo) {
        if (isPausd) {
            pausedNotificationBuilder.setContentTitle(suras[ayaIdInfo.getSuraNum() - 1]);
            pausedNotificationBuilder.setContentText(getString(R.string.aya_num, String.valueOf(ayaIdInfo.getAyaNumInSura())));
            notificationManager.notify(NOTIFICATION_ID, pausedNotificationBuilder.build());
        } else {
            notificationBuilder.setContentTitle(suras[ayaIdInfo.getSuraNum() - 1]);
            notificationBuilder.setContentText(getString(R.string.aya_num, String.valueOf(ayaIdInfo.getAyaNumInSura())));
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private void setActionsIntent() {

        int pendingIntentFlags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                (PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)
                : PendingIntent.FLAG_UPDATE_CURRENT;

        notificationIntent = PendingIntent.getActivity(getApplicationContext(), REQUEST_CODE_MAIN,
                getMainActivityIntent(), pendingIntentFlags);

        resumeIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_RESUME,
                getAudioIntent(ACTION_RESUME), pendingIntentFlags);

        nextIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_NEXT,
                getAudioIntent(LocaleUtils.isRTL(this) ? ACTION_PREVIOUS : ACTION_NEXT), pendingIntentFlags);

        prevIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_PREVIOUS,
                getAudioIntent(LocaleUtils.isRTL(this) ? ACTION_NEXT : ACTION_PREVIOUS), pendingIntentFlags);

        stopIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_STOP,
                getAudioIntent(ACTION_STOP), pendingIntentFlags);

        pauseIntent = PendingIntent.getService(getApplicationContext(), REQUEST_CODE_PAUSE,
                getAudioIntent(ACTION_PAUSE), pendingIntentFlags);

    }

    private Intent getMainActivityIntent() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra(FROM_NOTIFICATION, true);
        return mainIntent;
    }

    public Intent getAudioIntent(String action) {
        final Intent intent = new Intent(this, AyaAudioService.class);
        intent.setAction(action);
        intent.putExtra(AyaAudioService.FROM_NOTIFICATION, true);
        return intent;
    }

    private void setNotificationIcon() {
        if (notificationIcon == null) {
            try {
                Resources resources = getApplicationContext().getResources();
                Bitmap logo = BitmapFactory.decodeResource(resources, R.drawable.quranhub_logo_144dp);
                int iconWidth = logo.getWidth();
                int iconHeight = logo.getHeight();
                ColorDrawable cd = new ColorDrawable();
                Bitmap bitmap = Bitmap.createBitmap(iconWidth * 2, iconHeight * 2, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                cd.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                cd.draw(canvas);
                canvas.drawBitmap(logo, iconWidth / 2, iconHeight / 2, null);
                notificationIcon = bitmap;
            } catch (OutOfMemoryError oomError) {
                Log.d(TAG, "Notification icon OutOfMemoryError");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseAudio();
        stopAyaAudioDelay();
        SharedPrefsUtils.saveBoolean(this, SERVICE_RUNNING, false);
        SharedPrefsUtils.saveBoolean(this, AUDIO_PLAYING, false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
        getIntentExtra(intent);

        Log.d(TAG, "onStartCommand: " + currentAyaId);
        setAudioState(Objects.requireNonNull(intent.getAction()));

        // we don't want the service to restart if killed
        return START_NOT_STICKY;
    }

    private void getIntentExtra(Intent intent) {
        fromNotification = intent.getBooleanExtra(FROM_NOTIFICATION, false);
        if (!fromNotification) {
            currentAyaId = intent.getIntExtra(AYA_ID_KEY, currentAyaId);
        }
        if (suraVersesNumberArrayList == null) {
            suraVersesNumberArrayList = intent.getParcelableArrayListExtra(SURA_VERSES_KEY);
            suras = getResources().getStringArray(R.array.sura_name);
            setAyaIdInfo();
        }
    }

    private void setAyaIdInfo() {
        ayaIdInfoArrayList = new ArrayList<>();
        for (SuraVersesNumber suraVersesNumber : suraVersesNumberArrayList) {
            for (int i = 1; i <= suraVersesNumber.getAyas(); i++) {
                ayaIdInfoArrayList.add(new AyaIdInfo(i, suraVersesNumber.getId()));
            }
        }
    }

    // handle service actions and update audio state depend on sending action
    private void setAudioState(String action) {
        SharedPrefsUtils.saveBoolean(this, SERVICE_RUNNING, true);
        if (action.equals(ACTION_PLAY)) {
            checkSelectedAyaInRepeat();
            checkAyaAudioDownloaded(currentAyaId);
        } else if (action.equals(ACTION_PAUSE)) {
            Log.d(TAG, "pause " + currentAyaId);
            updateNotificationContent(true, ayaIdInfoArrayList.get(currentAyaId - 1));
            updateNotificationState(true);
            EventBus.getDefault().post(new AudioStateEvent(AudioStateEvent.State.PAUSED));
            pauseAudio();
        } else if (action.equals(ACTION_RESUME)) {
            Log.d(TAG, "resume: " + currentAyaId);
            updateNotificationState(false);
            EventBus.getDefault().post(new AudioStateEvent(AudioStateEvent.State.RESUME));
            playAudio();
        } else if (action.equals(ACTION_NEXT) && currentAyaId != Constants.Quran.NUM_OF_VERSES + 1) {
            EventBus.getDefault().post(new AudioStateEvent(AudioStateEvent.State.PLAY_NEXT));
            Log.d(TAG, "next: " + currentAyaId);
            if (fromNotification)
                ++currentAyaId;
            checkSelectedAyaInRepeat();
            checkAyaAudioDownloaded(currentAyaId);
        } else if (action.equals(ACTION_PREVIOUS) && currentAyaId != 0) {
            EventBus.getDefault().post(new AudioStateEvent(AudioStateEvent.State.PLAY_PREV));
            if (fromNotification)
                --currentAyaId;
            Log.d(TAG, "prev: " + currentAyaId);
            checkSelectedAyaInRepeat();
            checkAyaAudioDownloaded(currentAyaId);
        } else if (action.equals(ACTION_STOP)) {
            Log.d(TAG, "stop " + currentAyaId);
            EventBus.getDefault().post(new AudioStateEvent(AudioStateEvent.State.STOP));
            releaseAudio();
            stopSelf();
        }
    }

    public void releaseAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public boolean isAudioPlaying() {
        try {
            return mediaPlayer.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public void pauseAudio() {
        if (mediaPlayer != null && isAudioPlaying()) {
            try {
                mediaPlayer.pause();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void playAudio() {
        if (mediaPlayer != null && !isAudioPlaying()) {
            try {
                mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        EventBus.getDefault().post(new AudioStateEvent(AudioStateEvent.State.PLAYING));
        mp.start();
    }

    private void checkSelectedAyaInRepeat() {
        if (SharedRepeatModel.isIsRepeatModelChanged()) {
            currentAyaRepeatNumber = 1;
            currentGroupRepeatNumber = 1;
            SharedRepeatModel.setIsRepeatModelChanged(false);
        }
        RepeatModel repeatModel = SharedRepeatModel.getRepeatModel();
        if (repeatModel != null && (repeatModel.getFromAyaId() > currentAyaId || repeatModel.getToAyaId() < currentAyaId)) {
            currentGroupRepeatNumber = 1;
            currentAyaRepeatNumber = 1;
            SharedRepeatModel.setRepeatModel(null);
        }
    }

    public void stopAyaAudioDelay() {
        if (playAudioHandler != null && playAudioDelayRunnable != null) {
            playAudioHandler.removeCallbacks(playAudioDelayRunnable);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (currentAyaId != Constants.Quran.NUM_OF_VERSES) {
            checkSelectedAyaInRepeat();
            RepeatModel repeatModel = SharedRepeatModel.getRepeatModel();
            Log.d(TAG, "completed " + currentAyaId);
            if (repeatModel != null && currentAyaRepeatNumber != repeatModel.getAyaRepeatNum()) {
                stopAudio();
                ++currentAyaRepeatNumber;
                if (repeatModel.getDelayTime() > 0) {
                    setAudioDelay(AYA_REPEAT_CASE, repeatModel.getDelayTime());
                } else {
                    checkFileAudioExist(currentAudioPath);
                }
            } else if (repeatModel != null && currentGroupRepeatNumber != repeatModel.getGroupRepeatNum()
                    && currentAyaId == repeatModel.getToAyaId()) {
                ++currentGroupRepeatNumber;
                if (repeatModel.getDelayTime() > 0) {
                    setAudioDelay(GROUP_REPEAT_CASE, repeatModel.getDelayTime());
                } else {
                    EventBus.getDefault().post(new AudioStateEvent(AudioStateEvent.State.GROUP_REPEAT_COMPLETED));
                }


            } else {
                if (repeatModel != null && repeatModel.getDelayTime() > 0)
                    setAudioDelay(NEXT_AYA_CASE, repeatModel.getDelayTime());
                else
                    playNextAya();
            }
        }
    }

    private void playNextAya() {
        EventBus.getDefault().post(new AudioStateEvent(AudioStateEvent.State.COMPLETED));
        ++currentAyaId;
        checkAyaAudioDownloaded(currentAyaId);
    }

    private void setAudioDelay(int ayaRepeatCase, int audioDelay) {
        if (playAudioHandler == null) {
            playAudioHandler = new Handler();
        }

        switch (ayaRepeatCase) {
            case AYA_REPEAT_CASE:
                playAudioDelayRunnable = () -> checkFileAudioExist(currentAudioPath);
                break;
            case GROUP_REPEAT_CASE:
                playAudioDelayRunnable = () -> EventBus.getDefault().post(new AudioStateEvent(AudioStateEvent.State.GROUP_REPEAT_COMPLETED));
                break;
            case NEXT_AYA_CASE:
                playAudioDelayRunnable = () -> playNextAya();
                break;

        }
        playAudioHandler.postDelayed(playAudioDelayRunnable, audioDelay * 1000);
    }


    public void stopAudio() {
        try {
            mediaPlayer.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("CheckResult")
    public void checkAyaAudioDownloaded(int ayaId) {
        SharedPrefsUtils.saveInteger(this, AYA_ID_KEY, ayaId);
        currentAyaRepeatNumber = 1;
        stopAudio();
        stopAyaAudioDelay();
        String sheikhId = AppPreferencesManager.getReciterSheikhSetting(this);
        int recitationId = AppPreferencesManager.getRecitationSetting(this);
        userDatabase.getQuranAudioDao()
                .getAyaAudioPath(ayaId, recitationId, sheikhId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    result = getApplicationContext().getExternalFilesDir(null) + result;
                    checkFileAudioExist(result);
                }, error -> {
                    EventBus.getDefault().post(new AudioStateEvent(AudioStateEvent.State.NOT_DOWNLOADED));
                });
    }


    private void checkFileAudioExist(String audioPath) {
        if (audioPath != null) {
            File audioFile = new File(audioPath);
            if (audioFile.exists()) {
                try {
                    mediaPlayer.setDataSource(audioPath);
                    mediaPlayer.prepare();
                    SharedPrefsUtils.saveBoolean(this, AUDIO_PLAYING, true);
                    currentAudioPath = audioPath;
                    updateNotificationContent(false, ayaIdInfoArrayList.get(currentAyaId - 1));
                } catch (Exception e) {
                    Log.d(TAG, "checkFileAudioExist: Exception");
                }
            } else {
                EventBus.getDefault().post(new AudioStateEvent(AudioStateEvent.State.NOT_DOWNLOADED));
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
