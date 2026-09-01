package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.data.Constants
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.ui.mushaf.interactor.TafseerInteractor
import app.quranhub.ui.mushaf.interactor.TafseerInteractorImp
import app.quranhub.ui.mushaf.model.TafseerModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TafseerViewModel(application: Application) : AndroidViewModel(application) {

    data class TafseerUiState(
        val loading: Boolean = true,
        val tafseerItems: List<TafseerModel> = emptyList(),
        val bookName: String? = null
    )

    sealed interface TafseerEvent {
        data object NoDownloadedBooks : TafseerEvent
        data object DataLoadFailed : TafseerEvent
    }

    private val interactor: TafseerInteractor = TafseerInteractorImp(application)

    private var currentBookId: String? = AppPreferencesManager.getQuranTranslationBook(application)
    private var currentLanguage: String =
        AppPreferencesManager.getQuranTranslationLanguage(application)
    private var bookDbName: String? = null
    private var loadedSuraNumber = UNSET_SURA_NUMBER

    private val _uiState = MutableStateFlow(TafseerUiState())
    val uiState: StateFlow<TafseerUiState> = _uiState.asStateFlow()

    private val _tafseerEvents = Channel<TafseerEvent>(Channel.BUFFERED)
    val tafseerEvents: Flow<TafseerEvent> = _tafseerEvents.receiveAsFlow()

    private var loadJob: Job? = null

    fun setSelectedBook(bookDbName: String?, bookName: String?) {
        if (bookDbName != null && this.bookDbName == null) {
            this.bookDbName = bookDbName
        }
        if (bookName != null && _uiState.value.bookName == null) {
            _uiState.update { it.copy(bookName = bookName) }
        }
    }

    fun loadInitialTafseers(suraNumber: Int) {
        if (suraNumber == loadedSuraNumber) return
        loadTafseers(suraNumber)
    }

    fun onSuraSelected(suraNumber: Int) {
        loadTafseers(suraNumber)
    }

    fun onBookSelected(bookDbName: String, bookId: String, bookName: String) {
        this.bookDbName = bookDbName
        currentBookId = bookId
        _uiState.update { it.copy(bookName = bookName) }
        if (loadedSuraNumber != UNSET_SURA_NUMBER) {
            loadTafseers(loadedSuraNumber)
        }
    }

    fun onTranslationLanguageChanged(languageCode: String) {
        currentLanguage = languageCode
    }

    private fun loadTafseers(suraNumber: Int) {
        loadedSuraNumber = suraNumber
        loadJob?.cancel()
        _uiState.update { it.copy(loading = true) }
        loadJob = viewModelScope.launch {
            if (currentBookId != null) {
                loadBookTafseers(suraNumber)
            } else if (currentLanguage == Constants.Language.ARABIC_CODE) {
                loadDefaultTafseers(suraNumber)
            } else {
                _uiState.update { it.copy(loading = false, tafseerItems = emptyList()) }
                _tafseerEvents.send(TafseerEvent.NoDownloadedBooks)
            }
        }
    }

    // get aya tafseer from the default book of the mushaf database ("EL-Meyser")
    private suspend fun loadDefaultTafseers(suraNumber: Int) {
        try {
            emitTafseerItems(interactor.getSuraTafseers(suraNumber).first())
        } catch (e: Exception) {
            onLoadFailed()
        }
    }

    private suspend fun loadBookTafseers(suraNumber: Int) {
        try {
            val ayasTafseer = interactor.getSuraTafseers(suraNumber).first()
            interactor.initTranslationDB(bookDbName)
            val translations = interactor.getSuraBookTafseers(suraNumber).first()
            emitTafseerItems(
                if (translations.isEmpty() || ayasTafseer.isEmpty()) {
                    emptyList()
                } else {
                    TafseerModel.map(translations, ayasTafseer)
                }
            )
        } catch (e: Exception) {
            onLoadFailed()
        }
    }

    private suspend fun emitTafseerItems(tafseerItems: List<TafseerModel>) {
        _uiState.update { it.copy(loading = false, tafseerItems = tafseerItems) }
        if (tafseerItems.isEmpty()) {
            _tafseerEvents.send(TafseerEvent.DataLoadFailed)
        }
    }

    private suspend fun onLoadFailed() {
        _uiState.update { it.copy(loading = false) }
        _tafseerEvents.send(TafseerEvent.DataLoadFailed)
    }

    companion object {
        private const val UNSET_SURA_NUMBER = -1
    }
}
