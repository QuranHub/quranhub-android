package app.quranhub.ui.downloads_manager.viewmodel

import android.app.Application
import android.util.Log
import app.quranhub.R
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.data.service.QuranAudioDownloaderService
import app.quranhub.ui.downloads_manager.model.DisplayableDownload
import app.quranhub.util.QuranAudioDeleteUtils.deleteSuraAudio
import app.quranhub.util.QuranAudioDownloadUtils.registerReciterRecitation
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DownloadsSurasViewModel(
    application: Application,
    private val recitationId: Int,
    private val reciterId: String
) : BaseDownloadsViewModel(application) {

    override suspend fun loadDownloads(): List<DisplayableDownload> {
        val context = appContext
        val suras = context.resources.getStringArray(R.array.sura_name)
        val quranAudioDao = UserDatabase.getInstance(context).quranAudioDao
        val displayableDownloadList: MutableList<DisplayableDownload> = ArrayList()
        for (i in suras.indices) {
            val suraName = suras[i]
            val displayableDownload = DisplayableDownload(suraName)
            val suraId = i + 1
            val isDownloadable = quranAudioDao
                .getForSura(recitationId, reciterId, suraId)
                .isEmpty()
            displayableDownload.isDownloadable = isDownloadable
            displayableDownload.isDeletable = !isDownloadable
            displayableDownloadList.add(displayableDownload)
        }
        return displayableDownloadList
    }

    fun deleteSura(position: Int) {
        viewModelScope.launch {
            try {
                deleteSuraAudio(appContext, recitationId, reciterId, position + 1)
                refresh()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete sura audio", e)
            }
        }
    }

    fun downloadSura(position: Int) {
        viewModelScope.launch {
            try {
                val suraId = position + 1
                registerReciterRecitation(appContext, recitationId, reciterId)
                val recitationIdPreference =
                    AppPreferencesManager.getRecitationSetting(appContext)
                if (recitationIdPreference == recitationId) {
                    AppPreferencesManager.persistReciterSheikhSetting(appContext, reciterId)
                }
                QuranAudioDownloaderService.downloadSura(appContext, recitationId, reciterId, suraId)
                emitEvent(DownloadsEvent.DownloadStarted)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start sura download", e)
            }
        }
    }

    companion object {
        private val TAG = DownloadsSurasViewModel::class.java.simpleName
    }
}
