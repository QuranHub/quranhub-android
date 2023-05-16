package app.quranhub.ui.mushaf.audio_manager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.ui.base.BaseService
import app.quranhub.ui.main.MainActivity
import app.quranhub.ui.mushaf.model.AyaIdInfo
import app.quranhub.ui.mushaf.model.SuraVersesNumber
import app.quranhub.util.LocaleUtils.isRTL
import app.quranhub.util.SharedPrefsUtils.saveBoolean
import app.quranhub.util.SharedPrefsUtils.saveInteger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.io.File

class AyaAudioService : BaseService(), OnPreparedListener, OnCompletionListener {

    private var playAudioDelayRunnable: Runnable? = null
    private var playAudioHandler: Handler? = null
    private var currentAyaRepeatNumber = 1
    private var currentGroupRepeatNumber = 1
    private var currentAudioPath: String? = null
    private var fromNotification = false
    private var mediaPlayer: MediaPlayer? = null
    private var notificationIcon: Bitmap? = null
    private var notificationIntent: PendingIntent? = null
    private var resumeIntent: PendingIntent? = null
    private var nextIntent: PendingIntent? = null
    private var prevIntent: PendingIntent? = null
    private var stopIntent: PendingIntent? = null
    private var pauseIntent: PendingIntent? = null
    private var userDatabase: UserDatabase? = null
    private var currentAyaId = 0
    private var suraVersesNumberArrayList: ArrayList<SuraVersesNumber>? = null
    private var ayaIdInfoArrayList: MutableList<AyaIdInfo>? = null
    private var suras: Array<String> = arrayOf()
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var pausedNotificationBuilder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        createNotification()
        initPlayer()
        userDatabase = UserDatabase.getInstance(this)
    }

    private fun initPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setOnPreparedListener(this)
        mediaPlayer!!.setOnCompletionListener(this)
    }

    private fun createNotification() {
        createNotificationChannel()
        setNotificationIcon()
        setActionsIntent()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (notificationBuilder == null) {
            notificationBuilder = NotificationCompat.Builder(
                this, NOTIFICATION_CHANNEL_ID
            )
            notificationBuilder!!.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.audio_playing))
                .setContentTitle(getString(R.string.sura))
                .setLargeIcon(notificationIcon)
                .setSmallIcon(R.drawable.play_ayha_action_white_ic)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(notificationIntent)
                .addAction(
                    R.drawable.player_fast_rewind_white_ic,
                    getString(R.string.prev),
                    prevIntent
                )
                .addAction(R.drawable.ic_pause, getString(R.string.pause), pauseIntent)
                .addAction(
                    R.drawable.player_fast_forward_white_ic,
                    getString(R.string.next),
                    nextIntent
                )
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                )
        }
        if (pausedNotificationBuilder == null) {
            pausedNotificationBuilder = NotificationCompat.Builder(
                this, NOTIFICATION_CHANNEL_ID
            )
            pausedNotificationBuilder!!.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.audio_playing))
                .setContentTitle(getString(R.string.sura))
                .setLargeIcon(notificationIcon)
                .setSmallIcon(R.drawable.play_ayha_action_white_ic)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(notificationIntent)
                .addAction(R.drawable.ic_new_close, getString(R.string.stop), stopIntent)
                .addAction(
                    R.drawable.player_play_white_ic,
                    getString(R.string.resume),
                    resumeIntent
                )
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1)
                )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.audio_playing)
            val description = getString(R.string.audio_playing_desc)
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW
            )
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotificationState(isPausd: Boolean) {
        saveBoolean(this, AUDIO_PLAYING, !isPausd)
        if (isPausd) {
            notificationManager!!.notify(NOTIFICATION_ID, pausedNotificationBuilder!!.build())
        } else {
            notificationManager!!.notify(NOTIFICATION_ID, notificationBuilder!!.build())
        }
    }

    private fun updateNotificationContent(isPaused: Boolean, ayaIdInfo: AyaIdInfo) {
        if (isPaused) {
            pausedNotificationBuilder!!.setContentTitle(suras[ayaIdInfo.suraNum - 1])
            pausedNotificationBuilder!!.setContentText(
                getString(
                    R.string.aya_num,
                    ayaIdInfo.ayaNumInSura.toString()
                )
            )
            notificationManager!!.notify(NOTIFICATION_ID, pausedNotificationBuilder!!.build())
        } else {
            notificationBuilder!!.setContentTitle(suras[ayaIdInfo.suraNum - 1])
            notificationBuilder!!.setContentText(
                getString(
                    R.string.aya_num,
                    ayaIdInfo.ayaNumInSura.toString()
                )
            )
            notificationManager!!.notify(NOTIFICATION_ID, notificationBuilder!!.build())
        }
    }

    private fun setActionsIntent() {
        val pendingIntentFlags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        notificationIntent = PendingIntent.getActivity(
            applicationContext, REQUEST_CODE_MAIN,
            mainActivityIntent, pendingIntentFlags
        )
        resumeIntent = PendingIntent.getService(
            applicationContext, REQUEST_CODE_RESUME,
            getAudioIntent(ACTION_RESUME), pendingIntentFlags
        )
        nextIntent = PendingIntent.getService(
            applicationContext, REQUEST_CODE_NEXT,
            getAudioIntent(if (isRTL(this)) ACTION_PREVIOUS else ACTION_NEXT), pendingIntentFlags
        )
        prevIntent = PendingIntent.getService(
            applicationContext, REQUEST_CODE_PREVIOUS,
            getAudioIntent(if (isRTL(this)) ACTION_NEXT else ACTION_PREVIOUS), pendingIntentFlags
        )
        stopIntent = PendingIntent.getService(
            applicationContext, REQUEST_CODE_STOP,
            getAudioIntent(ACTION_STOP), pendingIntentFlags
        )
        pauseIntent = PendingIntent.getService(
            applicationContext, REQUEST_CODE_PAUSE,
            getAudioIntent(ACTION_PAUSE), pendingIntentFlags
        )
    }

    private val mainActivityIntent: Intent
        get() {
            val mainIntent = Intent(this, MainActivity::class.java)
            mainIntent.putExtra(FROM_NOTIFICATION, true)
            return mainIntent
        }

    fun getAudioIntent(action: String?): Intent {
        val intent = Intent(this, AyaAudioService::class.java)
        intent.action = action
        intent.putExtra(FROM_NOTIFICATION, true)
        return intent
    }

    private fun setNotificationIcon() {
        if (notificationIcon == null) {
            try {
                val resources = applicationContext.resources
                val logo = BitmapFactory.decodeResource(resources, R.drawable.quranhub_logo_144dp)
                val iconWidth = logo.width
                val iconHeight = logo.height
                val cd = ColorDrawable()
                val bitmap =
                    Bitmap.createBitmap(iconWidth * 2, iconHeight * 2, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                cd.setBounds(0, 0, canvas.width, canvas.height)
                cd.draw(canvas)
                canvas.drawBitmap(logo, (iconWidth / 2).toFloat(), (iconHeight / 2).toFloat(), null)
                notificationIcon = bitmap
            } catch (oomError: OutOfMemoryError) {
                Log.d(TAG, "Notification icon OutOfMemoryError")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseAudio()
        stopAyaAudioDelay()
        saveBoolean(this, SERVICE_RUNNING, false)
        saveBoolean(this, AUDIO_PLAYING, false)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notificationBuilder!!.build())
        getIntentExtra(intent)
        Log.d(TAG, "onStartCommand: $currentAyaId")
        setAudioState(intent.action!!)

        // we don't want the service to restart if killed
        return START_NOT_STICKY
    }

    private fun getIntentExtra(intent: Intent) {
        fromNotification = intent.getBooleanExtra(FROM_NOTIFICATION, false)
        if (!fromNotification) {
            currentAyaId = intent.getIntExtra(AYA_ID_KEY, currentAyaId)
        }
        if (suraVersesNumberArrayList == null) {
            suraVersesNumberArrayList = intent.getParcelableArrayListExtra(SURA_VERSES_KEY)
            suras = resources.getStringArray(R.array.sura_name)
            setAyaIdInfo()
        }
    }

    private fun setAyaIdInfo() {
        suraVersesNumberArrayList?.let {
            ayaIdInfoArrayList = ArrayList()
            for (suraVersesNumber in it) {
                for (i in 1..suraVersesNumber.ayas) {
                    ayaIdInfoArrayList!!.add(AyaIdInfo(i, suraVersesNumber.id))
                }
            }
        }
    }

    // handle service actions and update audio state depend on sending action
    private fun setAudioState(action: String) {
        saveBoolean(this, SERVICE_RUNNING, true)
        if (action == ACTION_PLAY) {
            checkSelectedAyaInRepeat()
            checkAyaAudioDownloaded(currentAyaId)
        } else if (action == ACTION_PAUSE) {
            Log.d(TAG, "pause $currentAyaId")
            ayaIdInfoArrayList?.getOrNull(currentAyaId - 1)?.let {
                updateNotificationContent(true, it)
            }
            updateNotificationState(true)
            EventBus.getDefault().post(AudioStateEvent(AudioStateEvent.State.PAUSED))
            pauseAudio()
        } else if (action == ACTION_RESUME) {
            Log.d(TAG, "resume: $currentAyaId")
            updateNotificationState(false)
            EventBus.getDefault().post(AudioStateEvent(AudioStateEvent.State.RESUME))
            playAudio()
        } else if (action == ACTION_NEXT && currentAyaId != Constants.Quran.NUM_OF_VERSES + 1) {
            EventBus.getDefault().post(AudioStateEvent(AudioStateEvent.State.PLAY_NEXT))
            Log.d(TAG, "next: $currentAyaId")
            if (fromNotification) ++currentAyaId
            checkSelectedAyaInRepeat()
            checkAyaAudioDownloaded(currentAyaId)
        } else if (action == ACTION_PREVIOUS && currentAyaId != 0) {
            EventBus.getDefault().post(AudioStateEvent(AudioStateEvent.State.PLAY_PREV))
            if (fromNotification) --currentAyaId
            Log.d(TAG, "prev: $currentAyaId")
            checkSelectedAyaInRepeat()
            checkAyaAudioDownloaded(currentAyaId)
        } else if (action == ACTION_STOP) {
            Log.d(TAG, "stop $currentAyaId")
            EventBus.getDefault().post(AudioStateEvent(AudioStateEvent.State.STOP))
            releaseAudio()
            stopSelf()
        }
    }

    fun releaseAudio() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }
    }

    val isAudioPlaying: Boolean
        get() = try {
            mediaPlayer!!.isPlaying
        } catch (e: Exception) {
            false
        }

    fun pauseAudio() {
        if (mediaPlayer != null && isAudioPlaying) {
            try {
                mediaPlayer!!.pause()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playAudio() {
        if (mediaPlayer != null && !isAudioPlaying) {
            try {
                mediaPlayer!!.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        EventBus.getDefault().post(AudioStateEvent(AudioStateEvent.State.PLAYING))
        mp.start()
    }

    private fun checkSelectedAyaInRepeat() {
        if (SharedRepeatModel.isRepeatModelChanged) {
            currentAyaRepeatNumber = 1
            currentGroupRepeatNumber = 1
            SharedRepeatModel.isRepeatModelChanged = false
        }
        val repeatModel = SharedRepeatModel.repeatModel
        if (repeatModel != null && (repeatModel.fromAyaId > currentAyaId || repeatModel.toAyaId < currentAyaId)) {
            currentGroupRepeatNumber = 1
            currentAyaRepeatNumber = 1
            SharedRepeatModel.repeatModel = null
        }
    }

    fun stopAyaAudioDelay() {
        if (playAudioHandler != null && playAudioDelayRunnable != null) {
            playAudioHandler!!.removeCallbacks(playAudioDelayRunnable!!)
        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        if (currentAyaId != Constants.Quran.NUM_OF_VERSES) {
            checkSelectedAyaInRepeat()
            val repeatModel = SharedRepeatModel.repeatModel
            Log.d(TAG, "completed $currentAyaId")
            if (repeatModel != null && currentAyaRepeatNumber != repeatModel.ayaRepeatNum) {
                stopAudio()
                ++currentAyaRepeatNumber
                if (repeatModel.delayTime > 0) {
                    setAudioDelay(AYA_REPEAT_CASE, repeatModel.delayTime)
                } else {
                    checkFileAudioExist(currentAudioPath)
                }
            } else if (repeatModel != null && currentGroupRepeatNumber != repeatModel.groupRepeatNum && currentAyaId == repeatModel.toAyaId) {
                ++currentGroupRepeatNumber
                if (repeatModel.delayTime > 0) {
                    setAudioDelay(GROUP_REPEAT_CASE, repeatModel.delayTime)
                } else {
                    EventBus.getDefault()
                        .post(AudioStateEvent(AudioStateEvent.State.GROUP_REPEAT_COMPLETED))
                }
            } else {
                if (repeatModel != null && repeatModel.delayTime > 0) setAudioDelay(
                    NEXT_AYA_CASE,
                    repeatModel.delayTime
                ) else playNextAya()
            }
        }
    }

    private fun playNextAya() {
        EventBus.getDefault().post(AudioStateEvent(AudioStateEvent.State.COMPLETED))
        ++currentAyaId
        checkAyaAudioDownloaded(currentAyaId)
    }

    private fun setAudioDelay(ayaRepeatCase: Int, audioDelay: Int) {
        if (playAudioHandler == null) {
            playAudioHandler = Handler()
        }
        when (ayaRepeatCase) {
            AYA_REPEAT_CASE -> playAudioDelayRunnable =
                Runnable { checkFileAudioExist(currentAudioPath) }

            GROUP_REPEAT_CASE -> playAudioDelayRunnable = Runnable {
                EventBus.getDefault()
                    .post(AudioStateEvent(AudioStateEvent.State.GROUP_REPEAT_COMPLETED))
            }

            NEXT_AYA_CASE -> playAudioDelayRunnable = Runnable { playNextAya() }
        }
        playAudioHandler!!.postDelayed(playAudioDelayRunnable!!, (audioDelay * 1000).toLong())
    }

    fun stopAudio() {
        try {
            mediaPlayer!!.reset()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("CheckResult")
    fun checkAyaAudioDownloaded(ayaId: Int) {
        saveInteger(this, AYA_ID_KEY, ayaId)
        currentAyaRepeatNumber = 1
        stopAudio()
        stopAyaAudioDelay()
        val sheikhId = AppPreferencesManager.getReciterSheikhSetting(this)
        val recitationId = AppPreferencesManager.getRecitationSetting(this)
        userDatabase!!.quranAudioDao
            .getAyaAudioPath(ayaId, recitationId, sheikhId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result: String ->
                checkFileAudioExist(
                    applicationContext.getExternalFilesDir(null).toString() + result
                )
            }) {
                EventBus.getDefault().post(AudioStateEvent(AudioStateEvent.State.NOT_DOWNLOADED))
            }
    }

    private fun checkFileAudioExist(audioPath: String?) {
        if (audioPath != null) {
            val audioFile = File(audioPath)
            if (audioFile.exists()) {
                try {
                    mediaPlayer!!.setDataSource(audioPath)
                    mediaPlayer!!.prepare()
                    saveBoolean(this, AUDIO_PLAYING, true)
                    currentAudioPath = audioPath
                    updateNotificationContent(false, ayaIdInfoArrayList!![currentAyaId - 1])
                } catch (e: Exception) {
                    Log.d(TAG, "checkFileAudioExist: Exception")
                }
            } else {
                EventBus.getDefault().post(AudioStateEvent(AudioStateEvent.State.NOT_DOWNLOADED))
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    companion object {

        private const val TAG = "AyaAudioService.service"

        private const val NOTIFICATION_CHANNEL_ID = "AyaAudioService.NOTIFICATION_CHANNEL_ID"
        private const val NOTIFICATION_ID = 2

        const val ACTION_PLAY = "PLAY"
        const val ACTION_RESUME = "RESUME"
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_STOP = "STOP"
        const val ACTION_NEXT = "NEXT"
        const val ACTION_PREVIOUS = "PREVIOUS"

        private const val REQUEST_CODE_MAIN = 0
        private const val REQUEST_CODE_PREVIOUS = 1
        private const val REQUEST_CODE_PAUSE = 2
        private const val REQUEST_CODE_NEXT = 3
        private const val REQUEST_CODE_STOP = 4
        private const val REQUEST_CODE_RESUME = 5

        private const val AYA_REPEAT_CASE = 1
        private const val GROUP_REPEAT_CASE = 2
        private const val NEXT_AYA_CASE = 4

        const val AYA_ID_KEY = "AYA_ID_KEY"

        const val FROM_NOTIFICATION = "FROM_NOTIFICATION"

        const val SURA_VERSES_KEY = "SURA_VERSES_KEY"

        const val SERVICE_RUNNING = "SERVICE_RUNNING"

        const val AUDIO_PLAYING = "AUDIO_PLAYING"
    }
}