package app.quranhub.ui.downloads_manager.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.ReciterRecitation
import app.quranhub.data.service.QuranAudioDownloaderService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Quran audio download-amount dialog: tracks the chosen
 * download amount & sura, registers the reciter's recitation in the local
 * database and starts the download.
 */
class AudioDownloadAmountViewModel(
    application: Application,
    private val recitationId: Int,
    private val reciterId: String,
    initialSuraId: Int
) : AndroidViewModel(application) {

    sealed interface AudioDownloadAmountEvent {
        data object DownloadStarted : AudioDownloadAmountEvent
    }

    data class AudioDownloadAmountUiState(
        val selectedOption: Int = OPTION_DOWNLOAD_SURA,
        val suraId: Int = 1
    )

    private val appContext = application

    private val _uiState = MutableStateFlow(AudioDownloadAmountUiState(suraId = initialSuraId))
    val uiState: StateFlow<AudioDownloadAmountUiState> = _uiState.asStateFlow()

    private val _events = Channel<AudioDownloadAmountEvent>(Channel.BUFFERED)
    val events: Flow<AudioDownloadAmountEvent> = _events.receiveAsFlow()

    fun onSuraSelected(suraId: Int) {
        _uiState.update { it.copy(selectedOption = OPTION_DOWNLOAD_SURA, suraId = suraId) }
    }

    fun onSuraDownloadOptionSelected() {
        _uiState.update { it.copy(selectedOption = OPTION_DOWNLOAD_SURA) }
    }

    fun onDownloadAllOptionSelected() {
        _uiState.update { it.copy(selectedOption = OPTION_DOWNLOAD_ALL) }
    }

    fun startDownload() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Store SheikhRecitation for the downloaded recitation & reciter in DB
                    val userDatabase = UserDatabase.getInstance(appContext)
                    if (userDatabase.reciterRecitationDao[recitationId, reciterId] == null) {
                        userDatabase.reciterRecitationDao
                            .insert(
                                ReciterRecitation(
                                    recitationId = recitationId,
                                    reciterId = reciterId
                                )
                            )
                    }
                }

                if (_uiState.value.selectedOption == OPTION_DOWNLOAD_SURA) {
                    QuranAudioDownloaderService.downloadSura(
                        appContext, recitationId, reciterId, _uiState.value.suraId
                    )
                } else {
                    QuranAudioDownloaderService.downloadQuran(
                        appContext, recitationId, reciterId
                    )
                }
                _events.send(AudioDownloadAmountEvent.DownloadStarted)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start Quran audio download", e)
            }
        }
    }

    companion object {
        private val TAG = AudioDownloadAmountViewModel::class.java.simpleName

        private const val OPTION_DOWNLOAD_SURA = 0
        private const val OPTION_DOWNLOAD_ALL = 1
    }
}
