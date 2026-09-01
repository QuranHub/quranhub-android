package app.quranhub.ui.mushaf.interactor

import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel

interface BookmarksInteractor {
    suspend fun getBookmarks(): List<AyaBookmark>
    suspend fun getBookmarkTypes(): List<BookmarkType>
    suspend fun getHizbQuarterForAya(ayaId: Int): HizbQuarterDataModel?
    suspend fun deleteBookmark(bookmarkId: Int)
    suspend fun changeBookmarkType(bookmarkId: Int, bookmarkTypeId: Int)
}
