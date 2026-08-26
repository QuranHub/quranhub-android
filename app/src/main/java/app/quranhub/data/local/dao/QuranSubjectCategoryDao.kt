package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import app.quranhub.data.local.entity.QuranSubjectCategory
import io.reactivex.Single

@Dao
interface QuranSubjectCategoryDao {
    @Query("SELECT * FROM QuranSubjectCategory")
    fun getAll(): Single<List<QuranSubjectCategory>>

    @Query("SELECT * FROM QuranSubjectCategory WHERE id IN(:ids)")
    fun getAllByIds(vararg ids: Int): List<QuranSubjectCategory?>?
}