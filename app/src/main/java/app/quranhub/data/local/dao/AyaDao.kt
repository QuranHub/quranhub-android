package app.quranhub.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import app.quranhub.data.local.entity.Aya
import app.quranhub.ui.mushaf.model.MyNoteModel
import app.quranhub.ui.mushaf.model.PageSuras
import app.quranhub.ui.mushaf.model.SearchModel
import app.quranhub.ui.mushaf.model.TafseerModel
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow

@Dao
interface AyaDao {

    @Query("SELECT * FROM Aya")
    fun getAll(): List<Aya?>?

    @Query("SELECT * FROM Aya WHERE id IN (:ayaIds)")
    fun getAllByIds(vararg ayaIds: Int): List<Aya?>?

    @Query("SELECT * FROM Aya WHERE id=:ayaId")
    fun findById(ayaId: Int): Single<Aya>

    @Query("SELECT * FROM Aya WHERE id=:ayaId")
    fun findAyaById(ayaId: Int): Aya?

    @Query("SELECT * FROM Aya WHERE page=:pageNum")
    suspend fun getAllInPage(pageNum: Int): List<Aya>

    @Query("SELECT * FROM Aya WHERE page=:page AND id=:ayaId LIMIT 1")
    fun getPageAya(page: Int, ayaId: Int): Aya?

    @Query("select text, tafseer, pure_text from aya WHERE sura=:suraNumber")
    fun getPageTafseers(suraNumber: Int): Flow<List<TafseerModel>>

    @Query("SELECT * FROM Aya WHERE page=:pageNum LIMIT 1")
    fun getFirstAyaInPage(pageNum: Int): Aya?

    @Query("SELECT id, sura, pure_text, text, page, sura_aya, juz FROM Aya WHERE id IN (select aya from AyaQuranSubject where subject=:categoryId)")
    fun getCategoryAyas(categoryId: Int): Single<List<SearchModel>>

    @Query("SELECT id, sura, pure_text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :input || '%'")
    fun getSimpleSearchResult(input: String?): Single<List<SearchModel>>

    @Query("SELECT id, sura, pure_text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :input || '%' and sura=:suraNumber")
    fun getSuraSearchResult(input: String?, suraNumber: Int): Single<List<SearchModel>>

    @Query("SELECT id, sura, pure_text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :input || '%' and juz=:juzNumber")
    fun getJuzSearchResult(input: String?, juzNumber: Int): Single<List<SearchModel>>

    @Query("SELECT distinct sura FROM Aya where juz=:juz ")
    fun getSurasInChapter(juz: Int): LiveData<List<Int?>?>?

    @Query("SELECT id, sura, pure_text, text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :inputSearch || '%' and juz=:selectedJuz and sura=:selectedSura")
    fun getSuraJuzSearchResult(
        inputSearch: String?,
        selectedSura: Int,
        selectedJuz: Int
    ): Single<List<SearchModel>>

    @Query(
        "SELECT id, sura, pure_text, text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :inputSearch || '%' " +
                "and juz=:selectedJuz and " +
                "id between (select aya_from from hizbquarter where id=:startHezbInterval) AND (select aya_to from hizbquarter where id=:endHezbInterval)"
    )
    fun getJuzHezbSearchResult(
        inputSearch: String?,
        selectedJuz: Int,
        startHezbInterval: Int,
        endHezbInterval: Int
    ): Single<List<SearchModel>>

    @Query(
        "SELECT id, sura, pure_text, text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :inputSearch || '%' " +
                "and juz=:selectedJuz and sura=:selectedSura and " +
                "id between (select aya_from from hizbquarter where id=:startHezbInterval) AND (select aya_to from hizbquarter where id=:endHezbInterval)"
    )
    fun getSuraJuzHezbSearchResult(
        inputSearch: String?,
        selectedSura: Int,
        selectedJuz: Int,
        startHezbInterval: Int,
        endHezbInterval: Int
    ): Single<List<SearchModel>>

    @Query("select id, sura, sura_aya, pure_text,text, page from aya where id IN(:ayaIds)")
    suspend fun getNoteData(ayaIds: List<Int>): List<MyNoteModel>

    @Query("select DISTINCT (page), sura from aya ")
    fun getSuraPage(): Single<List<PageSuras>>

    @Query("select page from aya where id=:ayaId")
    fun getAyaPage(ayaId: Int): Single<Int>

    @Query("SELECT * FROM Aya where id=(SELECT MIN(id) FROM Aya WHERE sura=:sura)")
    fun getFirstAyaInSura(sura: Int): Aya?

    @Query("SELECT * FROM Aya where id=(SELECT MAX(id) FROM Aya WHERE sura=:sura)")
    fun getLastAyaInSura(sura: Int): Aya?
}