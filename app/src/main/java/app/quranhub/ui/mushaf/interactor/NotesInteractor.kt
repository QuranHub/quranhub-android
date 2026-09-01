package app.quranhub.ui.mushaf.interactor

import app.quranhub.data.local.entity.Note
import app.quranhub.ui.mushaf.model.DisplayedNote

interface NotesInteractor {
    suspend fun getNotes(): List<DisplayedNote>
    suspend fun editNote(note: Note)
    suspend fun deleteNote(ayaId: Int)
}
