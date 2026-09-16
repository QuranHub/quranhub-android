package app.quranhub.feature.mushaf.interactor

import android.content.Context
import app.quranhub.core.data.local.db.MushafDatabase
import app.quranhub.core.data.local.db.UserDatabase
import app.quranhub.core.data.local.entity.AyaBookmark
import app.quranhub.core.data.local.entity.BookmarkType
import app.quranhub.core.data.model.HizbQuarterDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BookmarksInteractorImp(context: Context) : BookmarksInteractor {

    private val userDatabase: UserDatabase = UserDatabase.getInstance(context.applicationContext)
    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context.applicationContext)

    override suspend fun getBookmarks(): List<AyaBookmark> {
        return userDatabase.bookmarkDao.getAllBookmarks()
    }

    override suspend fun getBookmarkTypes(): List<BookmarkType> {
        return userDatabase.bookmarkDao.getAllBookmarkTypes()
    }

    override suspend fun getHizbQuarterForAya(ayaId: Int): HizbQuarterDataModel? {
        return mushafDatabase.hizbQuarterDao.getHizbQuarterDataModelForAya(ayaId)
    }

    override suspend fun deleteBookmark(bookmarkId: Int) {
        withContext(Dispatchers.IO) {
            userDatabase.bookmarkDao.deleteAyaBookmark(bookmarkId)
        }
    }

    override suspend fun changeBookmarkType(bookmarkId: Int, bookmarkTypeId: Int) {
        withContext(Dispatchers.IO) {
            userDatabase.bookmarkDao.changeAyaBookmarkType(bookmarkId, bookmarkTypeId)
        }
    }
}
