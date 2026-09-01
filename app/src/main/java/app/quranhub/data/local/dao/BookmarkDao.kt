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
    fun insertBookmarkType(bookmarkType: BookmarkType)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAyaBookmark(ayaBookmark: AyaBookmark)

    @Query("select * from AyaBookmark")
    suspend fun getAllBookmarks(): List<AyaBookmark>

    @Query("select * from BookmarkType")
    fun getBookmarksType(): Single<List<BookmarkType>>

    @Query("select * from AyaBookmark where bookmarkTypeId=:id")
    fun getTypeBookmarks(id: Int): LiveData<List<AyaBookmark?>?>?

    @Query("select AyaBookmark.bookmarkTypeId, BookmarkType.colorIndex from AyaBookmark join BookmarkType on AyaBookmark.bookmarkTypeId=BookmarkType.typeId where bookmarkId=:id")
    suspend fun getBookmarkType(id: Int): BookmarkModel?

    @Query("delete from AyaBookmark where bookmarkId=:id")
    fun deleteAyaBookmark(id: Int)

    @Query("UPDATE AyaBookmark SET bookmarkTypeId=:bookmarkTypeId WHERE bookmarkId=:bookmarkId")
    fun changeAyaBookmarkType(bookmarkId: Int, bookmarkTypeId: Int)

    @Query("select * from BookmarkType")
    suspend fun getAllBookmarkTypes(): List<BookmarkType>
}