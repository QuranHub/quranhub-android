package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import app.quranhub.data.local.entity.Translation
import kotlinx.coroutines.flow.Flow
import io.reactivex.Single

@Dao
interface TranslationDao {

    @Query("SELECT * FROM translation")
    fun getAll(): Single<List<Translation>>

    @Query("SELECT * FROM translation where `sura`=:suraNumber")
    fun getAyasTafseer(suraNumber: Int): Flow<List<Translation>>

    @Query("SELECT text FROM translation WHERE `index`=:index")
    fun findByIndex(index: Int): Single<String>

    @Query("SELECT * FROM translation WHERE sura=:sura AND aya=:aya LIMIT 1")
    fun findForAya(sura: Int, aya: Int): Translation?
}