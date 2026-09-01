package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.R
import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.data.local.entity.Note
import app.quranhub.ui.mushaf.interactor.QuranPageInteractor
import app.quranhub.ui.mushaf.interactor.QuranPageInteractorImp
import app.quranhub.ui.mushaf.model.BookmarkModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuranPageViewModel(
    application: Application,
    private val pageNumber: Int
) : AndroidViewModel(application) {

    sealed interface QuranPageEvent {
        data class InitAyaLoaded(val aya: Aya, val previousAya: Aya?) : QuranPageEvent
        data object ShowBookmarkTypePicker : QuranPageEvent
        data object BookmarkRemoved : QuranPageEvent
        data class ShowMessage(val message: String) : QuranPageEvent
    }

    data class QuranPageUiState(
        val pageAyas: List<Aya> = emptyList(),
        val saving: Boolean = false,
        val selectedAya: Aya? = null,
        val selectedAyaBookmarkType: BookmarkModel? = null,
        val selectedAyaNote: Note? = null,
        val bookmarkTypes: List<BookmarkType> = emptyList(),
    )

    private val context: Application = application
    private val quranPageInteractor: QuranPageInteractor = QuranPageInteractorImp(application)

    private val _uiState = MutableStateFlow(QuranPageUiState())
    val uiState: StateFlow<QuranPageUiState> = _uiState.asStateFlow()

    private val _events = Channel<QuranPageEvent>(Channel.BUFFERED)
    val events: Flow<QuranPageEvent> = _events.receiveAsFlow()

    init {
        loadPageAyas()
    }

    private fun loadPageAyas() {
        viewModelScope.launch {
            try {
                val ayas = quranPageInteractor.getPageAyas(pageNumber)
                _uiState.update { it.copy(pageAyas = ayas) }
            } catch (e: Exception) {
                Log.e(TAG, "loadPageAyas error", e)
                _events.send(
                    QuranPageEvent.ShowMessage(context.getString(R.string.page_info_failed))
                )
            }
        }
    }

    /**
     * Loads the initial selected aya (and its previous aya) for drawing the
     * first shadow when the page opens (e.g. deep link from an index or
     * notification).
     */
    fun loadInitAya(pageNumber: Int, ayaId: Int) {
        viewModelScope.launch {
            try {
                val (aya, previousAya) = quranPageInteractor.getPageAyaWithPrevious(
                    pageNumber, ayaId
                )
                if (aya != null) {
                    _uiState.update { it.copy(selectedAya = aya) }
                    _events.send(QuranPageEvent.InitAyaLoaded(aya, previousAya))
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadInitAya error", e)
            }
        }
    }

    fun selectAya(aya: Aya) {
        _uiState.update {
            it.copy(selectedAya = aya, selectedAyaBookmarkType = null, selectedAyaNote = null)
        }
    }

    /**
     * Loads the bookmark type and note of an aya into the UI state; resets the
     * previous values first so a new selection never shows stale state.
     */
    fun loadAyaState(ayaId: Int) {
        _uiState.update {
            it.copy(selectedAyaBookmarkType = null, selectedAyaNote = null)
        }
        viewModelScope.launch {
            try {
                val bookmarkType = quranPageInteractor.getBookmarkType(ayaId)
                val note = quranPageInteractor.checkAyaNote(ayaId)
                _uiState.update {
                    it.copy(selectedAyaBookmarkType = bookmarkType, selectedAyaNote = note)
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadAyaState error", e)
            }
        }
    }

    fun insertAyaBookmark(bookmarkTypeId: Int) {
        val aya = _uiState.value.selectedAya ?: return
        viewModelScope.launch {
            setSaving(true)
            try {
                quranPageInteractor.insertAyaBookmark(AyaBookmark(aya.id, bookmarkTypeId, aya))
                _events.send(
                    QuranPageEvent.ShowMessage(
                        context.getString(R.string.success_insert_bookmark)
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "insertAyaBookmark error", e)
                _events.send(
                    QuranPageEvent.ShowMessage(
                        context.getString(R.string.insert_bookmark_failed)
                    )
                )
            } finally {
                setSaving(false)
            }
        }
    }

    fun addCustomBookmark(bookmarkTypeName: String, colorIndex: Int) {
        val aya = _uiState.value.selectedAya ?: return
        viewModelScope.launch {
            setSaving(true)
            try {
                val typeId = _uiState.value.bookmarkTypes.size + 1
                quranPageInteractor.insertCustomBookmark(
                    aya, BookmarkType(typeId, bookmarkTypeName, colorIndex)
                )
                _events.send(
                    QuranPageEvent.ShowMessage(
                        context.getString(R.string.success_insert_bookmark)
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "addCustomBookmark error", e)
                _events.send(
                    QuranPageEvent.ShowMessage(
                        context.getString(R.string.insert_bookmark_failed)
                    )
                )
            } finally {
                setSaving(false)
            }
        }
    }

    fun loadBookmarkTypes() {
        viewModelScope.launch {
            try {
                val types = quranPageInteractor.getBookmarkTypes()
                _uiState.update { it.copy(bookmarkTypes = types) }
                _events.send(QuranPageEvent.ShowBookmarkTypePicker)
            } catch (e: Exception) {
                Log.e(TAG, "loadBookmarkTypes error", e)
            }
        }
    }

    fun removeBookmark() {
        val aya = _uiState.value.selectedAya ?: return
        viewModelScope.launch {
            setSaving(true)
            try {
                quranPageInteractor.removeBookmark(aya.id)
                _events.send(QuranPageEvent.BookmarkRemoved)
            } catch (e: Exception) {
                Log.e(TAG, "removeBookmark error", e)
                _events.send(
                    QuranPageEvent.ShowMessage(
                        context.getString(R.string.bookmark_failed_removed)
                    )
                )
            } finally {
                setSaving(false)
            }
        }
    }

    fun saveNote(note: Note) {
        viewModelScope.launch {
            setSaving(true)
            try {
                quranPageInteractor.addNote(note)
            } catch (e: Exception) {
                Log.e(TAG, "saveNote error", e)
            } finally {
                setSaving(false)
            }
        }
    }

    private fun setSaving(saving: Boolean) {
        _uiState.update { it.copy(saving = saving) }
    }

    companion object {
        private val TAG = QuranPageViewModel::class.java.simpleName
    }
}
