package app.quranhub.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.quranhub.data.local.entity.Book

@Dao
interface BookDao {
    @Query("select * from Book")
    fun getAllTranslations(): LiveData<List<Book?>?>?

    @Query("update Book set downloadStatus=:type, downloadId=:downloadId WHERE id=:id")
    fun updateDownlodedTranslation(id: Int, type: Int, downloadId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDownloadedTranslation(models: List<Book?>?)

    @Query("UPDATE Book SET downloadStatus=:status where downloadId=:downloadId")
    fun updateFinishedDownload(downloadId: Long, status: Int)
}