package app.quranhub.ui.mushaf.interactor

import android.content.Context
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.Note
import app.quranhub.ui.mushaf.model.DisplayedNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesInteractorImp(context: Context) : NotesInteractor {

    private val userDatabase: UserDatabase = UserDatabase.getInstance(context.applicationContext)
    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context.applicationContext)

    override suspend fun getNotes(): List<DisplayedNote> {
        val notes = userDatabase.noteDao.getAllNotes()
        if (notes.isEmpty()) {
            return emptyList()
        }
        val ayaIds = notes.map { it.ayaId }
        val noteDataById = mushafDatabase.ayaDao.getNoteData(ayaIds).associateBy { it.id }
        return notes.mapNotNull { note ->
            noteDataById[note.ayaId]?.let {
                DisplayedNote(
                    note.ayaId, note.noteType, note.noteText, note.noteRecorderPath,
                    it.sura, it.suraAya, it.pureText, it.text, it.page
                )
            }
        }
    }

    override suspend fun editNote(note: Note) {
        withContext(Dispatchers.IO) {
            userDatabase.noteDao.insertNote(note)
        }
    }

    override suspend fun deleteNote(ayaId: Int) {
        withContext(Dispatchers.IO) {
            userDatabase.noteDao.deleteNote(ayaId)
        }
    }
}
