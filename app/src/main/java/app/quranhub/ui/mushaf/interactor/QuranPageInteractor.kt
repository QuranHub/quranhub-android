package app.quranhub.ui.mushaf.interactor

import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.data.local.entity.Note
import app.quranhub.ui.mushaf.model.BookmarkModel

interface QuranPageInteractor {
    fun getPageAyaWithPrevious(pageNumber: Int, ayaId: Int)
    fun getPageAyas(page: Int)
    fun getBookmarkType(ayaId: Int)
    fun insertAyaBookmark(ayaBookmark: AyaBookmark)
    fun removeBookmark(ayaId: Int)
    fun addNote(note: Note)
    fun checkAyaNote(ayaId: Int)
    fun getBookmarkTypes()
    fun insertCustomBookmark(currentAya: Aya, type: BookmarkType)

    interface ResultListener {
        fun onGetAyaWithPrevious(aya: Aya?, previousAya: Aya?)
        fun onGetPageAyas(ayaList: List<Aya>)
        fun onGetBookmarkType(bookmarkType: BookmarkModel)
        fun onSuccessRemoveBookmark()
        fun showMessage(message: String)
        fun onSuccessAddNote()
        fun onGetAyaNote(note: Note)
        fun onGetBookmarkTypes(bookmarkTypes: List<BookmarkType>)
    }
}
