package app.quranhub.ui.downloads_manager.viewmodel

import android.app.Application
import android.util.Log
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.Reciter
import app.quranhub.data.local.entity.ReciterRecitation
import app.quranhub.data.repository.RecitationsRepository
import app.quranhub.ui.downloads_manager.model.DisplayableDownload
import app.quranhub.util.QuranAudioDeleteUtils.deleteReciterAudio
import app.quranhub.util.QuranAudioDownloadUtils.registerReciterRecitation
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DownloadsRecitersViewModel(application: Application, private val recitationId: Int) :
    BaseDownloadsViewModel(application) {

    private val recitationsRepository = RecitationsRepository()

    private var reciters: List<Reciter> = emptyList()

    fun reciterAt(position: Int): Reciter = reciters[position]

    override suspend fun loadDownloads(): List<DisplayableDownload> {
        val context = appContext

        val recitationKey = Constants.Recitation.keyForRecitation(recitationId)

        reciters = try {
            val reciterModels =
                recitationsRepository.getRecitersForRecitation(recitationKey)
            reciterModels.map {
                Reciter(
                    it.id,
                    it.getLocalizedName(context),
                    it.getLocalizedNationality(context),
                    it.audioBaseUrl
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve reciters from RecitationsRepository.")
            retrieveLocalReciters()
        }

        // process reciters list
        val displayableDownloadsList: MutableList<DisplayableDownload> = mutableListOf()
        val userDatabase = UserDatabase.getInstance(context)
        for (r in reciters) {
            val displayableDownload = DisplayableDownload(
                r.name
            )
            val downloadedSurasIds = userDatabase.reciterRecitationDao
                .getSurasIdsForReciterInRecitation(recitationId, r.id)
            displayableDownload.downloadedAmount =
                context.getString(R.string.downloaded_amount_suras, downloadedSurasIds.size)
            displayableDownload.isDownloadable = downloadedSurasIds.size < 114
            displayableDownload.isDeletable = downloadedSurasIds.isNotEmpty()
            displayableDownloadsList.add(displayableDownload)
        }
        return displayableDownloadsList
    }

    private fun retrieveLocalReciters(): List<Reciter> {
        return UserDatabase.getInstance(appContext)
            .reciterDao.getAllForRecitation(recitationId)
    }

    fun deleteReciter(position: Int) {
        viewModelScope.launch {
            try {
                deleteReciterAudio(appContext, recitationId, reciters[position].id)
                refresh()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete reciter audio", e)
            }
        }
    }

    /**
     * Registers the reciter (if needed) in the local database, then asks the
     * screen to open the audio download-amount dialog for this reciter.
     */
    fun onDownloadItem(position: Int) {
        viewModelScope.launch {
            try {
                val reciter = reciters[position]
                val userDatabase = UserDatabase.getInstance(appContext)
                if (userDatabase.reciterDao.getById(reciter.id) == null) {
                    userDatabase.reciterDao.insert(reciter)
                }
                registerReciterRecitation(appContext, recitationId, reciter.id)
                emitEvent(
                    DownloadsEvent.OpenAudioDownloadAmountDialog(recitationId, reciter.id)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register reciter for download", e)
            }
        }
    }

    companion object {
        private val TAG = DownloadsRecitersViewModel::class.java.simpleName
    }
}
