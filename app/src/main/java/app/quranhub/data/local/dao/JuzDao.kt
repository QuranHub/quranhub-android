package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import app.quranhub.data.local.entity.Juz

@Dao
interface JuzDao {
    @Query("SELECT * FROM Juz")
    fun getAll(): List<Juz?>?

    @Query("SELECT * FROM Juz WHERE id=:id")
    fun getById(id: Int): Juz?
}