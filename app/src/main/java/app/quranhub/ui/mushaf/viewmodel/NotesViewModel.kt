package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.R
import app.quranhub.data.local.entity.Note
import app.quranhub.ui.mushaf.interactor.NotesInteractor
import app.quranhub.ui.mushaf.interactor.NotesInteractorImp
import app.quranhub.ui.mushaf.model.DisplayedNote
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface NotesEvent {
        data class ShowError(val message: String) : NotesEvent
    }

    data class NotesUiState(
        val loading: Boolean = true,
        val notes: List<DisplayedNote> = emptyList(),
        val isEditMode: Boolean = false,
        val searchQuery: String = "",
        val filterType: Int = 0,
    )

    private val context: Application = application
    private val interactor: NotesInteractor = NotesInteractorImp(application)

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _notesEvents = Channel<NotesEvent>(Channel.BUFFERED)
    val notesEvents: Flow<NotesEvent> = _notesEvents.receiveAsFlow()

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            try {
                val displayedNotes = interactor.getNotes()
                _uiState.update { it.copy(loading = false, notes = displayedNotes) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false) }
                _notesEvents.send(NotesEvent.ShowError(context.getString(R.string.data_failed)))
            }
        }
    }

    fun onNoteEditClicked() {
        _uiState.update { it.copy(isEditMode = true) }
    }

    fun onFinishEditClicked() {
        _uiState.update { it.copy(isEditMode = false) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onFilterTypeSelected(filterType: Int) {
        _uiState.update { it.copy(filterType = filterType) }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            try {
                interactor.editNote(note)
                loadNotes()
            } catch (e: Exception) {
                _notesEvents.send(NotesEvent.ShowError(context.getString(R.string.data_failed)))
            }
        }
    }

    fun deleteNote(ayaId: Int) {
        viewModelScope.launch {
            try {
                interactor.deleteNote(ayaId)
                loadNotes()
            } catch (e: Exception) {
                _notesEvents.send(NotesEvent.ShowError(context.getString(R.string.data_failed)))
            }
        }
    }
}
