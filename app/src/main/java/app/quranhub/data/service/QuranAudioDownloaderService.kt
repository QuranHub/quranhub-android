package app.quranhub.data.service

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.widget.Toast
import androidx.core.content.ContextCompat
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.QuranAudio
import app.quranhub.data.service.QuranAudioDownloaderService.DownloadFinishEvent
import app.quranhub.prdownloader_service.DownloadRequestInfo
import app.quranhub.prdownloader_service.PRDownloaderService
import app.quranhub.util.LocaleUtils
import app.quranhub.util.QuranAudioDownloadUtils
import app.quranhub.util.QuranAudioFileUtils
import com.downloader.Error
import com.downloader.Progress
import org.greenrobot.eventbus.EventBus

/**
 * `PRDownloaderService` for Quran audio files.
 *
 *
 * To start this service use one of [QuranAudioDownloaderService.downloadSura]
 * , [QuranAudioDownloaderService.downloadQuran] or
 * [QuranAudioDownloaderService.downloadAyaRange].
 *
 *
 *
 * You can subscribe to [DownloadFinishEvent] with `EventBus` to get notified when all
 * the downloads finish, either successfully or unsuccessfully, and this service stops.
 *
 *
 * @author Abdallah Abdelazim
 * @see PRDownloaderService
 */
class QuranAudioDownloaderService : PRDownloaderService() {

    override fun provideDownloadRequestInfos(startIntent: Intent): Array<DownloadRequestInfo> {
        val startAyaId = startIntent.getIntExtra(EXTRA_START_AYA_ID, -1)
        val endAyaId = startIntent.getIntExtra(EXTRA_END_AYA_ID, -1)
        val recitationId = startIntent.getIntExtra(EXTRA_RECITATION_ID, -1)
        val reciterId = startIntent.getStringExtra(EXTRA_RECITER_ID)
        if (startAyaId == -1 || endAyaId == -1 || recitationId == -1 || reciterId == null) {
            throw RuntimeException(
                "MISSING INTENT EXTRAS: You must put EXTRA_START_AYA_ID," +
                        " EXTRA_END_AYA_ID, EXTRA_RECITATION_ID & EXTRA_RECITER_ID intent extras to the" +
                        " start intent of QuranAudioDownloaderService."
            )
        }
        val ayaDao = MushafDatabase.getInstance(this).ayaDao
        val downloadRequestInfos: MutableList<DownloadRequestInfo> = ArrayList()
        for (ayaId in startAyaId..endAyaId) {
            val aya = ayaDao.findAyaById(ayaId)
            val urlPath = QuranAudioDownloadUtils.getDownloadUrlPath(
                recitationId, reciterId, aya.sura, aya.suraAya
            )
            val dirPath = QuranAudioFileUtils.getLocalDirPath(
                this,
                recitationId, reciterId
            )
            val extraInfo = Bundle()
            extraInfo.putInt(DRI_EXTRA_INFO_AYA_ID, ayaId)
            extraInfo.putInt(DRI_EXTRA_INFO_RECITATION_ID, recitationId)
            extraInfo.putString(DRI_EXTRA_INFO_RECITER_ID, reciterId)
            downloadRequestInfos.add(
                DownloadRequestInfo.Builder(urlPath!!, true)
                    .setDirPath(dirPath)
                    .setExtraInfo(extraInfo)
                    .build()
            )
        }
        return downloadRequestInfos.toTypedArray()
    }

    override fun onCreate() {
        LocaleUtils.initAppLanguage(this)
        super.onCreate()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleUtils.initAppLanguage(newBase))
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        init(
            Constants.STATIC_FILES_BASE_URL,
            null,
            getString(R.string.download_notification_title_quran_audio)
        )
    }

    override fun onDownloadStartOrResume(downloadRequestInfo: DownloadRequestInfo) {
        Log.d(TAG, "onDownloadStartOrResume :: downloadRequestInfo=$downloadRequestInfo")
    }

    override fun onDownloadPause(downloadRequestInfo: DownloadRequestInfo) {
        Log.d(TAG, "onDownloadPause :: downloadRequestInfo=$downloadRequestInfo")
    }

    override fun onDownloadCancel(downloadRequestInfo: DownloadRequestInfo) {
        Log.d(TAG, "onDownloadCancel :: downloadRequestInfo=$downloadRequestInfo")
    }

    override fun onDownloadProgress(
        downloadRequestInfo: DownloadRequestInfo,
        progress: Progress
    ) {
    }

    override fun onDownloadComplete(downloadRequestInfo: DownloadRequestInfo) {
        Log.d(TAG, "onDownloadComplete :: downloadRequestInfo=$downloadRequestInfo")
        object : Thread() {
            override fun run() {
                val recitationId = downloadRequestInfo.extraInfo!!.getInt(
                    DRI_EXTRA_INFO_RECITATION_ID
                )
                val reciterId = downloadRequestInfo.extraInfo!!.getString(
                    DRI_EXTRA_INFO_RECITER_ID
                )
                val ayaId = downloadRequestInfo.extraInfo!!.getInt(DRI_EXTRA_INFO_AYA_ID)
                val mushafDatabase = MushafDatabase.getInstance(this@QuranAudioDownloaderService)
                val userDatabase = UserDatabase.getInstance(this@QuranAudioDownloaderService)
                val aya = mushafDatabase.ayaDao.findAyaById(ayaId)
                val filePath = (QuranAudioFileUtils.getLocalRelativeDirPath(recitationId, reciterId)
                        + downloadRequestInfo.fileName)
                val sheikhRecitationId = userDatabase.reciterRecitationDao
                    .getSheikhRecitationId(recitationId, reciterId)
                val quranAudio = QuranAudio(
                    page = aya.page, sura = aya.sura, aya = aya.suraAya,
                    ayaId = ayaId, filePath = filePath, sheikhRecitationId = sheikhRecitationId
                )
                userDatabase.quranAudioDao.insert(quranAudio)
            }
        }.start()
    }

    override fun onDownloadError(downloadRequestInfo: DownloadRequestInfo, error: Error) {
        Log.e(
            TAG, "onDownloadError :: downloadRequestInfo=" + downloadRequestInfo +
                    " , error code=" + error.responseCode
        )
    }

    override fun onStop() {
        Log.d(TAG, "onStop")

//        Toast.makeText(this, R.string.toast_download_quran_audio_finished, Toast.LENGTH_SHORT).show();
        EventBus.getDefault().post(DownloadFinishEvent())
    }

    /**
     * `EventBus` event that gets posted when all the downloads finish, either successfully or
     * unsuccessfully, and [QuranAudioDownloaderService] stops.
     */
    class DownloadFinishEvent
    companion object {
        private val TAG = QuranAudioDownloaderService::class.java.simpleName
        private const val EXTRA_START_AYA_ID = "EXTRA_START_AYA_ID"
        private const val EXTRA_END_AYA_ID = "EXTRA_END_AYA_ID"
        private const val EXTRA_RECITATION_ID = "EXTRA_RECITATION_ID"
        private const val EXTRA_RECITER_ID = "EXTRA_RECITER_ID"
        private const val DRI_EXTRA_INFO_AYA_ID = "DRI_EXTRA_INFO_AYA_ID"
        private const val DRI_EXTRA_INFO_RECITATION_ID = "DRI_EXTRA_INFO_RECITATION_ID"
        private const val DRI_EXTRA_INFO_RECITER_ID = "DRI_EXTRA_INFO_RECITER_ID"

        @JvmStatic
        fun downloadSura(
            context: Context, recitationId: Int, reciterId: String?,
            suraId: Int
        ) {
            object : AsyncTask<Void?, Void?, Pair<Int?, Int?>?>() {
                override fun doInBackground(vararg params: Void?): Pair<Int?, Int?>? {
                    val ayaDao = MushafDatabase.getInstance(context).ayaDao
                    val startAyaId = ayaDao.getFirstAyaInSura(suraId).id
                    val endAyaId = ayaDao.getLastAyaInSura(suraId).id
                    return Pair(startAyaId, endAyaId)
                }

                override fun onPostExecute(ayaIdPair: Pair<Int?, Int?>?) {
                    downloadAyaRange(
                        context,
                        recitationId,
                        reciterId,
                        ayaIdPair!!.first!!,
                        ayaIdPair.second!!
                    )
                }
            }.execute()
        }

        @JvmStatic
        fun downloadQuran(context: Context, recitationId: Int, reciterId: String?) {
            downloadAyaRange(context, recitationId, reciterId, 1, 6236)
        }

        /**
         * Downloads Aya audio for the given recitation & reciter from `startAyaId`
         * to `endAyaId` inclusive.
         */
        fun downloadAyaRange(
            context: Context, recitationId: Int, reciterId: String?,
            startAyaId: Int, endAyaId: Int
        ) {
            if (reciterId != null) {
                val intent = Intent(context, QuranAudioDownloaderService::class.java)
                intent.action = ACTION_DOWNLOAD
                intent.putExtra(EXTRA_START_AYA_ID, startAyaId)
                intent.putExtra(EXTRA_END_AYA_ID, endAyaId)
                intent.putExtra(EXTRA_RECITATION_ID, recitationId)
                intent.putExtra(EXTRA_RECITER_ID, reciterId)
                ContextCompat.startForegroundService(context, intent)
            } else {
                Log.w(TAG, "'reciterId' arg is null. Service will not be started.")
                Toast.makeText(context, R.string.msg_download_no_reciter, Toast.LENGTH_SHORT).show()
            }
        }

        fun cancelAllDownloads(context: Context) {
            val intent = Intent(context, QuranAudioDownloaderService::class.java)
            intent.action = ACTION_CANCEL_ALL_DOWNLOADS
            ContextCompat.startForegroundService(context, intent)
        }
    }
}