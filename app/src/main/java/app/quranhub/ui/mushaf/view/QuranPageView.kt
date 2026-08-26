package app.quranhub.ui.mushaf.view

import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.data.local.entity.Note
import app.quranhub.ui.base.BaseView
import app.quranhub.ui.mushaf.model.BookmarkModel

interface QuranPageView : BaseView {
    fun drawInitAyaShadow(aya: Aya, previousAya: Aya?)
    fun onGetPageAya(ayaList: List<Aya>)
    fun onGetAyaBookmarkType(bookmarkModel: BookmarkModel)
    fun onSuccessRemoveBookmark()
    fun onAyaHasNote(note: Note)
    fun onGetAyaBookmarkTypes(bookmarkTypes: List<BookmarkType>)
}
