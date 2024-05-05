package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import app.quranhub.data.local.entity.Reciter
import app.quranhub.data.local.entity.ReciterRecitation

@Dao
interface ReciterRecitationDao {
    @Query("SELECT * FROM ReciterRecitation")
    fun getAll(): List<ReciterRecitation?>?

    @Query("SELECT * FROM ReciterRecitation where id IN (:sheikhRecitationsIds)")
    fun getAllByIds(sheikhRecitationsIds: IntArray?): List<ReciterRecitation?>?

    @Query("SELECT * FROM ReciterRecitation Where id=:sheikhRecitationId")
    fun getById(sheikhRecitationId: Int): ReciterRecitation?

    @Query("SELECT * FROM ReciterRecitation WHERE recitation_id=:recitationId AND reciter_id=:reciterId")
    operator fun get(recitationId: Int, reciterId: String?): ReciterRecitation?

    @Query("SELECT id FROM ReciterRecitation WHERE recitation_id=:recitationId AND reciter_id=:sheikhId")
    fun getSheikhRecitationId(recitationId: Int, sheikhId: String?): Int

    @Query(
        "SELECT COUNT(DISTINCT sr.reciter_id) FROM ReciterRecitation as sr JOIN QuranAudio as q " +
                "ON sr.id=q.sheikh_recitation_id WHERE sr.recitation_id=:recitationId"
    )
    fun getNumOfRecitersWithDownloads(recitationId: Int): Int

    @Query("SELECT s.* FROM Reciter as s JOIN ReciterRecitation as sr ON s.id=sr.reciter_id WHERE recitation_id=:recitationId")
    fun getRecitersForRecitation(recitationId: Int): List<Reciter?>?

    @Query(
        "SELECT DISTINCT sura FROM Reciter as s JOIN ReciterRecitation as sr JOIN QuranAudio as q " +
                "ON s.id=sr.reciter_id AND sr.id=q.sheikh_recitation_id WHERE sr.recitation_id=:recitationId " +
                "AND s.id=:reciterId"
    )
    fun getSurasIdsForReciterInRecitation(recitationId: Int, reciterId: String?): List<Int>

    @Insert
    fun insert(reciterRecitation: ReciterRecitation)

    @Insert
    fun insertAll(reciterRecitations: Array<ReciterRecitation>)

    @Delete
    fun delete(reciterRecitation: ReciterRecitation)

    @Query("DELETE FROM ReciterRecitation WHERE recitation_id=:recitationId AND reciter_id=:reciterId")
    fun delete(recitationId: Int, reciterId: String?)
}