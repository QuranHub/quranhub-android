package app.quranhub.feature.mushaf.interactor

import app.quranhub.core.data.local.entity.Note
import app.quranhub.feature.mushaf.model.DisplayedNote

interface NotesInteractor {
    suspend fun getNotes(): List<DisplayedNote>
    suspend fun editNote(note: Note)
    suspend fun deleteNote(ayaId: Int)
}
