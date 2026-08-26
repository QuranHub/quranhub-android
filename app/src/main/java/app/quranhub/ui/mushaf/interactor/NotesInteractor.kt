package app.quranhub.ui.mushaf.interactor

import androidx.lifecycle.LiveData
import app.quranhub.data.local.entity.Note
import app.quranhub.ui.mushaf.model.DisplayedNote

interface NotesInteractor {
    val notes: LiveData<List<DisplayedNote>>
    fun editNote(note: Note?)
    fun deleteNote(ayaId: Int)
}
