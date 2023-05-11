package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import app.quranhub.data.local.entity.Note
import app.quranhub.ui.mushaf.interactor.NotesInteractor
import app.quranhub.ui.mushaf.interactor.NotesInteractorImp
import app.quranhub.ui.mushaf.model.DisplayedNote

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val interactor: NotesInteractor
    private var notesLiveData: LiveData<List<DisplayedNote>>? = null
    val notes: MediatorLiveData<List<DisplayedNote>>

    init {
        interactor = NotesInteractorImp(application)
        notes = MediatorLiveData()
    }

    val allNotes: Unit
        get() {
            notesLiveData = interactor.notes
            notes.addSource(notesLiveData!!) { displayedNotes: List<DisplayedNote> ->
                notes.value = displayedNotes
                notes.removeSource(notesLiveData!!)
            }
        }

    fun updateNote(note: Note?) {
        interactor.editNote(note)
    }

    fun deleteNote(ayaId: Int) {
        interactor.deleteNote(ayaId)
    }
}