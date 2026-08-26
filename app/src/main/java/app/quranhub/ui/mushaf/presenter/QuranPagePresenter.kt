package app.quranhub.ui.mushaf.presenter

import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.data.local.entity.Note
import app.quranhub.ui.base.BasePresenter
import app.quranhub.ui.base.BaseView

interface QuranPagePresenter<T : BaseView> : BasePresenter<T> {
    fun getPageAyas(page: Int)
    fun insertAyaBookmark(ayaBookmark: AyaBookmark)
    fun removeBookmark(ayaId: Int)
    fun getAyaBookmarkType(ayaId: Int)
    fun drawInitAyaShadow(pageNumber: Int, ayaId: Int)
    fun handleQuranPageClick()
    fun addNote(note: Note)
    fun checkAyaHasNote(id: Int)
    fun getBookmarkTypes()
    fun insertCustomBookmark(currentAya: Aya, type: BookmarkType)
}
