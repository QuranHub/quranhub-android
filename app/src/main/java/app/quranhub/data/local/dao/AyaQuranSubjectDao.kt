package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import app.quranhub.data.local.entity.AyaQuranSubject

@Dao
interface AyaQuranSubjectDao {
    @Query("SELECT * FROM AyaQuranSubject WHERE id=:id")
    fun findById(id: Int): AyaQuranSubject?
}