package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import app.quranhub.data.local.entity.Reciter

@Dao
interface ReciterDao {
    @Query("SELECT * FROM Reciter")
    fun getAll(): List<Reciter?>?

    @Query("SELECT * FROM Reciter where id IN (:recitersIds)")
    fun getAllByIds(recitersIds: IntArray?): List<Reciter?>?

    @Query(
        "SELECT s.id, s.name, s.nationality, s.audio_base_url FROM Reciter as s JOIN ReciterRecitation as sr " +
                "ON s.id=sr.reciter_id WHERE sr.recitation_id=:recitationId"
    )
    fun getAllForRecitation(recitationId: Int): List<Reciter>

    @Query("SELECT * FROM Reciter Where id=:reciterId")
    fun getById(reciterId: String?): Reciter?

    @Insert
    fun insert(reciter: Reciter)

    @Insert
    fun insertAll(reciters: Array<Reciter>)

    @Delete
    fun delete(reciter: Reciter)

    @Delete
    fun deleteAll(reciters: Array<Reciter>)
}