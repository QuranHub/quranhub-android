package app.quranhub.feature.mushaf.interactor

import app.quranhub.core.data.local.entity.AyaBookmark
import app.quranhub.core.data.local.entity.BookmarkType
import app.quranhub.core.data.model.HizbQuarterDataModel

interface BookmarksInteractor {
    suspend fun getBookmarks(): List<AyaBookmark>
    suspend fun getBookmarkTypes(): List<BookmarkType>
    suspend fun getHizbQuarterForAya(ayaId: Int): HizbQuarterDataModel?
    suspend fun deleteBookmark(bookmarkId: Int)
    suspend fun changeBookmarkType(bookmarkId: Int, bookmarkTypeId: Int)
}
