package app.quranhub.ui.mushaf.interactor

import androidx.lifecycle.LiveData
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.data.local.entity.Sura
import app.quranhub.ui.mushaf.model.DisplayableBookmark

interface BookmarksInteractor {
    fun getSura(suraId: Int): Sura
    val allBookmarks: LiveData<List<DisplayableBookmark>>
    fun deleteBookmark(bookmarkId: Int)
    fun filterBookmarks(filterType: Int): LiveData<List<AyaBookmark>>
    fun changeBookmarkType(bookmarkId: Int, bookmarkTypeId: Int)
    val bookmarkTypes: LiveData<List<BookmarkType>>
}
