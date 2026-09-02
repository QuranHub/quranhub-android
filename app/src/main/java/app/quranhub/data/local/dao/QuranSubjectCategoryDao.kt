package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import app.quranhub.data.local.entity.QuranSubjectCategory

@Dao
interface QuranSubjectCategoryDao {
    @Query("SELECT * FROM QuranSubjectCategory")
    suspend fun getAll(): List<QuranSubjectCategory>

    @Query("SELECT * FROM QuranSubjectCategory WHERE id IN(:ids)")
    fun getAllByIds(vararg ids: Int): List<QuranSubjectCategory?>?
}