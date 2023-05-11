package app.quranhub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.AyaBookmark;
import app.quranhub.data.local.entity.BookmarkType;
import app.quranhub.ui.mushaf.model.BookmarkModel;
import io.reactivex.Single;

@Dao
public interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertBookmarkType(BookmarkType bookmarkType);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAyaBookmark(AyaBookmark ayaBookmark);

    @Query("select * from AyaBookmark")
    Single<List<AyaBookmark>> getAllBookmarks();

    @Query("select * from BookmarkType")
    Single<List<BookmarkType>> getBookmarksType();

    @Query("select * from AyaBookmark where bookmarkTypeId=:id")
    LiveData<List<AyaBookmark>> getTypeBookmarks(int id);

    @Query("select AyaBookmark.bookmarkTypeId, BookmarkType.colorIndex from AyaBookmark join BookmarkType on AyaBookmark.bookmarkTypeId=BookmarkType.typeId where bookmarkId=:id")
        // todo make query return bookmarktype and color index to set filter to icon by join statment
    Single<BookmarkModel> getBookmarkType(int id);

    @Query("delete from AyaBookmark where bookmarkId=:id")
    void deleteAyaBookmark(int id);

    @Query("select * from AyaBookmark where bookmarkTypeId=:filterId")
    LiveData<List<AyaBookmark>> getFilterBookmaks(int filterId);

    @Query("UPDATE AyaBookmark SET bookmarkTypeId=:bookmarkTypeId WHERE bookmarkId=:bookmarkId")
    void changeAyaBookmarkType(int bookmarkId, int bookmarkTypeId);

    @Query("select * from BookmarkType")
    Single<List<BookmarkType>> getBookmarkTypes();

    @Query("select * from BookmarkType")
    LiveData<List<BookmarkType>> getBookmarkTypesLiveData();
}
