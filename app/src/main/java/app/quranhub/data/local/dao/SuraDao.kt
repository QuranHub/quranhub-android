package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import app.quranhub.data.local.entity.Sura
import app.quranhub.ui.mushaf.model.QuranPageInfo
import app.quranhub.ui.mushaf.model.SuraIndexModel
import app.quranhub.ui.mushaf.model.SuraVersesNumber
import io.reactivex.Single

@Dao
interface SuraDao {
    @Query("SELECT * FROM Sura")
    fun getAll(): Single<List<Sura>>

    @Query("SELECT * FROM Sura WHERE id=:suraId")
    fun findById(suraId: Int): Sura?

    @Query("SELECT juz, sura from aya where page=:currentPage LIMIT 1")
    fun getQuranPageInfo(currentPage: Int): Single<QuranPageInfo>

    @Query("select sura.id, sura.ayas, sura.type, aya.juz, aya.page, aya.sura from sura join aya on aya.sura=sura.id and aya.sura_aya=1")
    suspend fun getSuraIndexInfo(): List<SuraIndexModel>

    @Query("select id, ayas from sura")
    fun getSuraVersesNumber(): Single<List<SuraVersesNumber>>
}