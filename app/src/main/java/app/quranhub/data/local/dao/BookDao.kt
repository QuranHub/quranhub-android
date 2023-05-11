package app.quranhub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.Book;

@Dao
public interface BookDao {

    @Query("select * from Book")
    LiveData<List<Book>> getAllTranslations();

    @Query("update Book set downloadStatus=:type, downloadId=:downloadId WHERE id=:id")
    void updateDownlodedTranslation(int id, int type, long downloadId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertDownloadedTranslation(List<Book> models);

    @Query("UPDATE Book SET downloadStatus=:status where downloadId=:downloadId")
    void updateFinishedDownload(long downloadId, int status);

}
