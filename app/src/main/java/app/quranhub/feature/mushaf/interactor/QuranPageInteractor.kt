package app.quranhub.feature.mushaf.interactor

import app.quranhub.core.data.local.entity.Aya
import app.quranhub.core.data.local.entity.AyaBookmark
import app.quranhub.core.data.local.entity.BookmarkType
import app.quranhub.core.data.local.entity.Note
import app.quranhub.core.data.model.BookmarkModel

interface QuranPageInteractor {
    suspend fun getPageAyaWithPrevious(pageNumber: Int, ayaId: Int): Pair<Aya?, Aya?>
    suspend fun getPageAyas(page: Int): List<Aya>
    suspend fun getBookmarkType(ayaId: Int): BookmarkModel?
    suspend fun insertAyaBookmark(ayaBookmark: AyaBookmark)
    suspend fun removeBookmark(ayaId: Int)
    suspend fun addNote(note: Note)
    suspend fun checkAyaNote(ayaId: Int): Note?
    suspend fun getBookmarkTypes(): List<BookmarkType>
    suspend fun insertCustomBookmark(currentAya: Aya, type: BookmarkType)
}
