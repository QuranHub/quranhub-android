package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import app.quranhub.data.local.entity.Recitation

@Dao
interface RecitationDao {
    @Query("SELECT * FROM Recitation")
    fun getAll(): List<Recitation?>?

    @Query("SELECT * FROM Recitation where id IN (:recitationsIds)")
    fun getAllByIds(recitationsIds: IntArray?): List<Recitation?>?

    @Query("SELECT * FROM Recitation Where id=:recitationId")
    fun getById(recitationId: Int): Recitation?

    @Insert
    fun insert(recitation: Recitation)

    @Insert
    fun insertAll(recitations: Array<Recitation>)

    @Delete
    fun delete(recitation: Recitation)
}