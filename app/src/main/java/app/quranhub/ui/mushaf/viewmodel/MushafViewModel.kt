package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.ui.mushaf.interactor.Mus7fInteractor
import app.quranhub.ui.mushaf.interactor.Mus7fInteractorImp
import app.quranhub.ui.mushaf.model.QuranPageInfo
import app.quranhub.ui.mushaf.model.SuraVersesNumber
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MushafViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface MushafEvent {
        data class AyaTafseerLoaded(val tafseer: String) : MushafEvent
        data class TranslationBookLoaded(val book: TranslationBook) : MushafEvent
        data object NoTranslationBooks : MushafEvent
        data class AyaRecorderFound(val recorderPath: String) : MushafEvent
        data class FromAyaPageLoaded(val page: Int) : MushafEvent
        data class NotificationAyaLoaded(val aya: Aya) : MushafEvent
        data class ShowMessage(val message: String) : MushafEvent
    }

    data class MushafUiState(
        val pageInfo: QuranPageInfo? = null,
        // 2D list including suras numbers in each page
        val pageSuras: ArrayList<ArrayList<Int>>? = null,
        val suraVersesNumbers: ArrayList<SuraVersesNumber>? = null,
        val nightMode: Boolean,
        val barsVisible: Boolean = true,
    )

    private val context: Application = application
    private val interactor: Mus7fInteractor = Mus7fInteractorImp(application)

    private val _uiState =
        MutableStateFlow(MushafUiState(nightMode = AppPreferencesManager.getNightModeSetting(application)))
    val uiState: StateFlow<MushafUiState> = _uiState.asStateFlow()

    private val _events = Channel<MushafEvent>(Channel.BUFFERED)
    val events: Flow<MushafEvent> = _events.receiveAsFlow()

    val quranPageZoomScaleFactor: Float
        get() = AppPreferencesManager.getQuranPageZoomScaleSetting(context)

    init {
        loadPageSuras()
        loadSuraNumofVerses()
    }

    fun loadPageInfo(currentPage: Int) {
        val adjustedPage = Constants.Quran.NUM_OF_PAGES - currentPage
        viewModelScope.launch {
            try {
                val pageInfo = interactor.getPageInfo(adjustedPage)
                if (pageInfo != null) {
                    _uiState.update { it.copy(pageInfo = pageInfo) }
                } else {
                    Log.d(TAG, "loadPageInfo: Page info not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadPageInfo: ", e)
                _events.send(MushafEvent.ShowMessage(context.getString(R.string.page_info_failed)))
            }
        }
    }

    private fun loadPageSuras() {
        viewModelScope.launch {
            try {
                val pageSuras = interactor.getPageSuras()
                _uiState.update { it.copy(pageSuras = pageSuras) }
            } catch (e: Exception) {
                Log.e(TAG, "loadPageSuras error", e)
            }
        }
    }

    private fun loadSuraNumofVerses() {
        viewModelScope.launch {
            try {
                val suraVersesNumbers = interactor.getSuraNumofVerses()
                _uiState.update { it.copy(suraVersesNumbers = suraVersesNumbers) }
            } catch (e: Exception) {
                Log.d(TAG, "Failed loadSuraNumofVerses: ")
            }
        }
    }

    fun toggleNightMode() {
        val newNightMode = !_uiState.value.nightMode
        AppPreferencesManager.persistNightModeSetting(context, newNightMode)
        _uiState.update { it.copy(nightMode = newNightMode) }
    }

    fun toggleBars() {
        _uiState.update { it.copy(barsVisible = !it.barsVisible) }
    }

    fun getAyaTafseer(ayaId: Int) {
        viewModelScope.launch {
            try {
                val tafseer = interactor.getAyaTafseer(ayaId)
                if (tafseer != null) {
                    _events.send(MushafEvent.AyaTafseerLoaded(tafseer))
                } else {
                    Log.d(TAG, "getAyaTafseer: Tafseer not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getAyaTafseer: ", e)
                _events.send(MushafEvent.ShowMessage(context.getString(R.string.page_info_failed)))
            }
        }
    }

    fun loadTafseerBook(currentTafsserId: String) {
        viewModelScope.launch {
            try {
                val book = interactor.getTafseerBook(currentTafsserId)
                if (book != null) {
                    interactor.initTranslationDB(book.databaseName)
                    _events.send(MushafEvent.TranslationBookLoaded(book))
                } else {
                    _events.send(MushafEvent.NoTranslationBooks)
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadTafseerBook error", e)
                _events.send(MushafEvent.NoTranslationBooks)
            }
        }
    }

    fun selectTranslationBook(book: TranslationBook) {
        interactor.initTranslationDB(book.databaseName)
        viewModelScope.launch {
            _events.send(MushafEvent.TranslationBookLoaded(book))
        }
    }

    fun checkAyaHasRecorder(id: Int) {
        viewModelScope.launch {
            try {
                val recorderPath = interactor.checkAyaHasRecorder(id)
                if (recorderPath != null) {
                    _events.send(MushafEvent.AyaRecorderFound(recorderPath))
                } else {
                    Log.d(TAG, "checkAyaHasRecorder: No recorder exist")
                }
            } catch (e: Exception) {
                Log.e(TAG, "checkAyaHasRecorder: Error", e)
            }
        }
    }

    fun saveRecorderPath(ayaId: Int, recorderPath: String) {
        viewModelScope.launch {
            try {
                interactor.saveRecorderPath(ayaId, recorderPath)
            } catch (e: Exception) {
                Log.e(TAG, "saveRecorderPath error", e)
            }
        }
    }

    fun deleteAyaVoiceRecorder(ayaId: Int) {
        viewModelScope.launch {
            try {
                interactor.deleteAyaVoiceRecorder(ayaId)
            } catch (e: Exception) {
                Log.e(TAG, "deleteAyaVoiceRecorder error", e)
            }
        }
    }

    fun getFromAyaPage(fromAya: Int) {
        viewModelScope.launch {
            try {
                val page = interactor.getFromAyaPage(fromAya)
                if (page != null) {
                    _events.send(MushafEvent.FromAyaPageLoaded(page))
                } else {
                    Log.d(TAG, "getFromAyaPage: Aya not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getFromAyaPage error", e)
            }
        }
    }

    fun getNotificationAya(ayaId: Int) {
        viewModelScope.launch {
            try {
                val aya = interactor.getAya(ayaId)
                if (aya != null) {
                    _events.send(MushafEvent.NotificationAyaLoaded(aya))
                } else {
                    Log.d(TAG, "getNotificationAya: Aya not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getNotificationAya error", e)
            }
        }
    }

    companion object {
        private val TAG = MushafViewModel::class.java.simpleName
    }
}
