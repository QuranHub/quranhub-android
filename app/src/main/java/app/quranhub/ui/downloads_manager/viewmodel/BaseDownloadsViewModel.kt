package app.quranhub.ui.downloads_manager.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.ui.downloads_manager.model.DisplayableDownload
import kotlinx.coroutines.channels.BufferOverflow
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
 * Base ViewModel for the download listing screens. Each screen loads its
 * [DisplayableDownload] listing once at creation (and on [refresh]), exposed
 * as StateFlow so the listing survives rotation.
 */
abstract class BaseDownloadsViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * One-time events emitted to the hosting screen.
     */
    sealed interface DownloadsEvent {
        data class OpenAudioDownloadAmountDialog(
            val recitationId: Int,
            val reciterId: String
        ) : DownloadsEvent

        data object DownloadStarted : DownloadsEvent
    }

    data class DownloadsUiState(
        val loading: Boolean = true,
        val downloads: List<DisplayableDownload> = emptyList()
    )

    protected val appContext: Context get() = getApplication()

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    private val _events = Channel<DownloadsEvent>(
        capacity = Channel.BUFFERED,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: Flow<DownloadsEvent> = _events.receiveAsFlow()

    init {
        refresh()
    }

    /**
     * Reloads the listing (used initially, on download completion and after
     * a delete).
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            try {
                val downloads = withContext(workDispatcher) { loadDownloads() }
                _uiState.update { it.copy(loading = false, downloads = downloads) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load downloads", e)
                _uiState.update { it.copy(loading = false, downloads = emptyList()) }
            }
        }
    }

    /** Runs on [workDispatcher]; pure data mapping, no UI access. */
    protected abstract suspend fun loadDownloads(): List<DisplayableDownload>

    protected suspend fun emitEvent(event: DownloadsEvent) {
        _events.send(event)
    }

    companion object {
        private val TAG = BaseDownloadsViewModel::class.java.simpleName

        protected val workDispatcher = kotlinx.coroutines.Dispatchers.IO
    }
}
