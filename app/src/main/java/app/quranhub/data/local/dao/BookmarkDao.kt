package app.quranhub.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.quranhub.data.local.entity.AyaBookmark
import app.quranhub.data.local.entity.BookmarkType
import app.quranhub.ui.mushaf.model.BookmarkModel
import io.reactivex.Single

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBookmarkType(bookmarkType: BookmarkType?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAyaBookmark(ayaBookmark: AyaBookmark?)

    @Query("select * from AyaBookmark")
    fun getAllBookmarks(): Single<List<AyaBookmark?>?>?

    @Query("select * from BookmarkType")
    fun getBookmarksType(): Single<List<BookmarkType?>?>?

    @Query("select * from AyaBookmark where bookmarkTypeId=:id")
    fun getTypeBookmarks(id: Int): LiveData<List<AyaBookmark?>?>?

    @Query("select AyaBookmark.bookmarkTypeId, BookmarkType.colorIndex from AyaBookmark join BookmarkType on AyaBookmark.bookmarkTypeId=BookmarkType.typeId where bookmarkId=:id")
    fun  // todo make query return bookmarktype and color index to set filter to icon by join statment
            getBookmarkType(id: Int): Single<BookmarkModel?>?

    @Query("delete from AyaBookmark where bookmarkId=:id")
    fun deleteAyaBookmark(id: Int)

    @Query("select * from AyaBookmark where bookmarkTypeId=:filterId")
    fun getFilterBookmaks(filterId: Int): LiveData<List<AyaBookmark?>?>?

    @Query("UPDATE AyaBookmark SET bookmarkTypeId=:bookmarkTypeId WHERE bookmarkId=:bookmarkId")
    fun changeAyaBookmarkType(bookmarkId: Int, bookmarkTypeId: Int)

    @Query("select * from BookmarkType")
    fun getBookmarkTypes(): Single<List<BookmarkType?>?>?

    @Query("select * from BookmarkType")
    fun getBookmarkTypesLiveData(): LiveData<List<BookmarkType?>?>?
}