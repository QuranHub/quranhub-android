package app.quranhub.ui.mushaf.interactor

import android.content.Context
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.data.local.entity.Note
import app.quranhub.ui.mushaf.model.BookmarkModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuranPageInteractorImp(context: Context) : QuranPageInteractor {

    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context)
    private val userDatabase: UserDatabase = UserDatabase.getInstance(context)

    override suspend fun getPageAyaWithPrevious(pageNumber: Int, ayaId: Int): Pair<Aya?, Aya?> =
        withContext(Dispatchers.IO) {
            val currentAya = mushafDatabase.ayaDao.getPageAya(pageNumber, ayaId)
            val previousAya = mushafDatabase.ayaDao.getPageAya(pageNumber, ayaId - 1)
            currentAya to previousAya
        }

    override suspend fun getPageAyas(page: Int): List<Aya> {
        return mushafDatabase.ayaDao.getAllInPage(page)
    }

    override suspend fun getBookmarkType(ayaId: Int): BookmarkModel? {
        return userDatabase.bookmarkDao.getBookmarkType(ayaId)
    }

    override suspend fun insertAyaBookmark(ayaBookmark: AyaBookmark) {
        withContext(Dispatchers.IO) {
            userDatabase.bookmarkDao.insertAyaBookmark(ayaBookmark)
        }
    }

    override suspend fun removeBookmark(ayaId: Int) {
        withContext(Dispatchers.IO) {
            userDatabase.bookmarkDao.deleteAyaBookmark(ayaId)
        }
    }

    override suspend fun addNote(note: Note) {
        withContext(Dispatchers.IO) {
            userDatabase.noteDao.insertNote(note)
        }
    }

    override suspend fun checkAyaNote(ayaId: Int): Note? {
        return userDatabase.noteDao.getAyaNote(ayaId)
    }

    override suspend fun getBookmarkTypes(): List<BookmarkType> {
        return userDatabase.bookmarkDao.getAllBookmarkTypes()
    }

    override suspend fun insertCustomBookmark(currentAya: Aya, type: BookmarkType) {
        withContext(Dispatchers.IO) {
            userDatabase.bookmarkDao.insertBookmarkType(type)
            userDatabase.bookmarkDao.insertAyaBookmark(
                AyaBookmark(currentAya.id, type.typeId, currentAya)
            )
        }
    }
}
