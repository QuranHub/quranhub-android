package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.R
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.ui.mushaf.dialogs.BookmarkEditDialog
import app.quranhub.ui.mushaf.interactor.BookmarksInteractor
import app.quranhub.ui.mushaf.interactor.BookmarksInteractorImp
import app.quranhub.ui.mushaf.model.DisplayableBookmark
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookmarksViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface BookmarksEvent {
        data object ListNotEditable : BookmarksEvent
        data class ShowError(val message: String) : BookmarksEvent
    }

    data class BookmarksUiState(
        val loading: Boolean = true,
        val bookmarks: List<DisplayableBookmark> = emptyList(),
        val bookmarkTypes: List<BookmarkType> = emptyList(),
        val isEditMode: Boolean = false,
        val isListEditable: Boolean = false,
        val searchQuery: String = "",
        val filterType: Int = BookmarkEditDialog.ALL_BOOKMARK_FILTER,
    )

    private val context: Application = application
    private val bookmarksInteractor: BookmarksInteractor = BookmarksInteractorImp(application)

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    private val _bookmarksEvents = Channel<BookmarksEvent>(Channel.BUFFERED)
    val bookmarksEvents: Flow<BookmarksEvent> = _bookmarksEvents.receiveAsFlow()

    init {
        loadBookmarks()
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            try {
                val ayaBookmarks = bookmarksInteractor.getBookmarks()
                val types = bookmarksInteractor.getBookmarkTypes()
                val displayableBookmarks = mapToDisplayableBookmarks(ayaBookmarks, types)
                _uiState.update {
                    it.copy(
                        loading = false,
                        bookmarks = displayableBookmarks,
                        bookmarkTypes = types,
                        isListEditable = displayableBookmarks.isNotEmpty()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false) }
                _bookmarksEvents.send(BookmarksEvent.ShowError(context.getString(R.string.data_failed)))
            }
        }
    }

    /**
     * Maps a List of AyaBookmark to a List of DisplayableBookmark, enriching each
     * bookmark with its hizb/quarter data (the former AsyncTask mapper, now
     * structured coroutines on viewModelScope).
     */
    private suspend fun mapToDisplayableBookmarks(
        ayaBookmarks: List<AyaBookmark>,
        types: List<BookmarkType>
    ): List<DisplayableBookmark> {
        val suraNames = context.resources.getStringArray(R.array.sura_name)
        return ayaBookmarks.map { ayaBookmark ->
            val hizbQuarterData = bookmarksInteractor.getHizbQuarterForAya(ayaBookmark.aya.id)
            DisplayableBookmark(
                bookmarkId = ayaBookmark.bookmarkId,
                bookmarkType = ayaBookmark.bookmarkTypeId,
                ayaContent = ayaBookmark.aya.pureText,
                ayaId = ayaBookmark.aya.id,
                suraAyaNumber = ayaBookmark.aya.suraAya,
                guz2Number = ayaBookmark.aya.juz,
                hizbNumber = hizbQuarterData?.hizb ?: 0,
                rub3Number = hizbQuarterData?.quarter ?: 0,
                suraName = suraNames.getOrNull(ayaBookmark.aya.sura - 1),
                pageNumber = ayaBookmark.aya.page,
                colorIndex = types.firstOrNull { it.typeId == ayaBookmark.bookmarkTypeId }?.colorIndex ?: 0
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onEditClicked() {
        if (!_uiState.value.isListEditable) {
            viewModelScope.launch {
                _bookmarksEvents.send(BookmarksEvent.ListNotEditable)
            }
        } else {
            _uiState.update { it.copy(isEditMode = true) }
        }
    }

    fun onFinishEditClicked() {
        _uiState.update { it.copy(isEditMode = false) }
    }

    fun onFilterTypeSelected(filterType: Int) {
        _uiState.update { it.copy(filterType = filterType) }
    }

    fun deleteBookmark(bookmarkId: Int) {
        viewModelScope.launch {
            try {
                bookmarksInteractor.deleteBookmark(bookmarkId)
                loadBookmarks()
            } catch (e: Exception) {
                _bookmarksEvents.send(BookmarksEvent.ShowError(context.getString(R.string.data_failed)))
            }
        }
    }

    fun changeBookmarkType(bookmarkId: Int, bookmarkTypeId: Int) {
        viewModelScope.launch {
            try {
                bookmarksInteractor.changeBookmarkType(bookmarkId, bookmarkTypeId)
                loadBookmarks()
            } catch (e: Exception) {
                _bookmarksEvents.send(BookmarksEvent.ShowError(context.getString(R.string.data_failed)))
            }
        }
    }
}
