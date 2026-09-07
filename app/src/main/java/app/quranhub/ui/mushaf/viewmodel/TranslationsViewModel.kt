package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.remote.TranslationDownloader
import app.quranhub.data.repository.TranslationsRepository
import app.quranhub.ui.mushaf.interactor.TranslationsInteractor
import app.quranhub.ui.mushaf.interactor.TranslationsInteractorImp
import app.quranhub.ui.mushaf.model.DisplayableTranslation
import app.quranhub.util.NetworkUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the translations library screen: merges the local (downloaded)
 * translation books with the remote (available) ones and owns the
 * translation downloaders, so the listing state survives rotation.
 */
class TranslationsViewModel(
    application: Application,
    private val languageCode: String?
) : AndroidViewModel(application) {

    sealed interface TranslationsEvent {
        data object FetchFailed : TranslationsEvent
        data object DownloadFailed : TranslationsEvent
    }

    data class TranslationsUiState(
        val loading: Boolean = true,
        val translations: List<DisplayableTranslation> = emptyList()
    )

    private val appContext = application

    private val translationsRepository = TranslationsRepository()
    private val translationsInteractor: TranslationsInteractor =
        TranslationsInteractorImp(application)

    private val downloaders = mutableMapOf<String, TranslationDownloader>()

    private val callback = object : TranslationDownloader.TranslationDownloadCallback {
        override fun onDownloadStarted(book: TranslationBook) {
            updateBook(book.id, NetworkUtil.STATUS_DOWNLOADING, book.downloadLevelPercentage)
        }

        override fun onDownloadProgress(book: TranslationBook, percent: Int) {
            updateBook(book.id, NetworkUtil.STATUS_DOWNLOADING, percent)
        }

        override fun onDownloadFinished(book: TranslationBook) {
            downloaders.remove(book.id)
            updateBook(book.id, NetworkUtil.STATUS_DOWNLOADED, 100)
        }

        override fun onDownloadCancelled(book: TranslationBook) {
            downloaders.remove(book.id)
            updateBook(book.id, NetworkUtil.STATUS_NOT_DOWNLOADED, 0)
        }

        override fun onDownloadFailed(book: TranslationBook) {
            downloaders.remove(book.id)
            updateBook(book.id, NetworkUtil.STATUS_NOT_DOWNLOADED, 0)
            notifyDownloadFailed()
        }
    }

    private val _uiState = MutableStateFlow(TranslationsUiState())
    val uiState: StateFlow<TranslationsUiState> = _uiState.asStateFlow()

    private val _events = Channel<TranslationsEvent>(Channel.BUFFERED)
    val events: Flow<TranslationsEvent> = _events.receiveAsFlow()

    // Latest remote (available) & local (downloaded) translation books
    private val remoteBooks = MutableStateFlow<List<TranslationBook>>(emptyList())
    private val localBooks = MutableStateFlow<List<TranslationBook>>(emptyList())

    init {
        observeLocalTranslationBooks()
        mergeListing()
        fetchTranslationBooks()
    }

    private fun observeLocalTranslationBooks() {
        viewModelScope.launch {
            translationsInteractor.getBooksForLanguage(languageCode)
                .collect { books -> localBooks.value = books }
        }
    }

    /** Merges remote & local books: downloaded books first, then remote ones not downloaded yet. */
    private fun mergeListing() {
        viewModelScope.launch {
            combine(remoteBooks, localBooks) { remote, local ->
                mergeBooks(remote, local)
            }.collect { merged ->
                _uiState.update { it.copy(translations = merged) }
            }
        }
    }

    private fun fetchTranslationBooks() {
        viewModelScope.launch {
            try {
                translationsRepository.getTranslationsForLanguage(languageCode!!)
                    .collect { translations ->
                        remoteBooks.value = translations
                        _uiState.update { it.copy(loading = false) }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching translations", e)
                _uiState.update { it.copy(loading = false) }
                _events.send(TranslationsEvent.FetchFailed)
            }
        }
    }

    fun downloadTranslation(translationBook: TranslationBook) {
        Log.d(TAG, "onDownloadTranslationClick: translationBook = $translationBook")
        val downloader = TranslationDownloader(translationBook, appContext, callback)
        downloaders[translationBook.id] = downloader
        updateBook(translationBook.id, NetworkUtil.STATUS_DOWNLOADING, 0)
        downloader.download()
    }

    fun cancelDownload(translationBook: TranslationBook) {
        Log.d(TAG, "onCancelDownloadTranslationClick: translationBook = $translationBook")
        downloaders.remove(translationBook.id)?.cancel()
        updateBook(translationBook.id, NetworkUtil.STATUS_NOT_DOWNLOADED, 0)
    }

    private fun mergeBooks(
        remote: List<TranslationBook>,
        local: List<TranslationBook>
    ): List<DisplayableTranslation> {
        val displayableTranslations: MutableList<DisplayableTranslation> = ArrayList()
        for (book in local) {
            displayableTranslations.add(DisplayableTranslation(overlayInFlightProgress(book)))
        }
        for (book in remote) {
            if (displayableTranslations.none { it.id == book.id }) {
                displayableTranslations.add(DisplayableTranslation(overlayInFlightProgress(book)))
            }
        }
        return displayableTranslations
    }

    private fun overlayInFlightProgress(book: TranslationBook): TranslationBook {
        val inProgress = downloaders[book.id] ?: return book
        return book.copy(
            downloadStatus = NetworkUtil.STATUS_DOWNLOADING,
            downloadLevelPercentage = inProgress.translationBook.downloadLevelPercentage
        )
    }

    private fun updateBook(id: String, status: Int, percentage: Int) {
        _uiState.update { state ->
            state.copy(
                translations = state.translations.map { item ->
                    if (item.id != id) item
                    else DisplayableTranslation(
                        item.translationBook.copy(
                            downloadStatus = status,
                            downloadLevelPercentage = percentage
                        )
                    )
                }
            )
        }
    }

    override fun onCleared() {
        // cancel the downloader network requests and their scopes; the
        // cleanup DB deletes still run via NonCancellable
        downloaders.values.forEach { it.cancel() }
        downloaders.clear()
        super.onCleared()
    }

    private fun notifyDownloadFailed() {
        viewModelScope.launch {
            _events.send(TranslationsEvent.DownloadFailed)
        }
    }

    companion object {
        private val TAG = TranslationsViewModel::class.java.simpleName
    }
}
