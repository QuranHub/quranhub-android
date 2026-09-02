package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import app.quranhub.data.local.entity.QuranSubject

@Dao
interface QuranSubjectDao {
    @Query("SELECT * FROM QuranSubject")
    suspend fun getAll(): List<QuranSubject>

    @Query("SELECT * FROM QuranSubject WHERE id IN(:ids)")
    fun getAllByIds(vararg ids: Int): List<QuranSubject?>?
}