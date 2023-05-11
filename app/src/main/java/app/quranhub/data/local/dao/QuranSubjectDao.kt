package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import app.quranhub.data.local.entity.QuranSubject
import io.reactivex.Single

@Dao
interface QuranSubjectDao {
    @Query("SELECT * FROM QuranSubject")
    fun getAll(): Single<List<QuranSubject?>?>?

    @Query("SELECT * FROM QuranSubject WHERE id IN(:ids)")
    fun getAllByIds(vararg ids: Int): List<QuranSubject?>?
}