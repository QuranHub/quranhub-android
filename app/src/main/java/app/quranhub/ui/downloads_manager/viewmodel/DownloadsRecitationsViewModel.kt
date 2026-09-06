package app.quranhub.ui.downloads_manager.viewmodel

import android.app.Application
import android.util.Log
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.ui.downloads_manager.model.DisplayableDownload
import app.quranhub.util.QuranAudioDeleteUtils.deleteRecitationAudio
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DownloadsRecitationsViewModel(application: Application) :
    BaseDownloadsViewModel(application) {

    override suspend fun loadDownloads(): List<DisplayableDownload> {
        val context = appContext

        val downloads: MutableList<DisplayableDownload> = ArrayList()

        for (recitationStringResId in Constants.Recitation.NAMES_STR_IDS) {
            downloads.add(DisplayableDownload(context.getString(recitationStringResId)))
        }

        // check if recitations are downloadable and/or deletable & the number of downloaded reciters each.
        val reciterRecitationDao = UserDatabase.getInstance(context).reciterRecitationDao
        for (i in downloads.indices) {
            val displayableDownload = downloads[i]
            val numOfDownloadedReciters = reciterRecitationDao.getNumOfRecitersWithDownloads(i)
            displayableDownload.downloadedAmount =
                context.getString(R.string.downloaded_reciters_num, numOfDownloadedReciters)
            displayableDownload.isDeletable = numOfDownloadedReciters > 0
            displayableDownload.isDownloadable = true // TODO check if it's not downloadable
        }
        return downloads
    }

    fun deleteRecitation(recitationId: Int) {
        viewModelScope.launch {
            try {
                deleteRecitationAudio(appContext, recitationId)
                refresh()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete recitation audio", e)
            }
        }
    }

    companion object {
        private val TAG = DownloadsRecitationsViewModel::class.java.simpleName
    }
}
