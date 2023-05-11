package app.quranhub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.Aya;
import app.quranhub.ui.mushaf.model.MyNoteModel;
import app.quranhub.ui.mushaf.model.PageSuras;
import app.quranhub.ui.mushaf.model.SearchModel;
import app.quranhub.ui.mushaf.model.TafseerModel;
import io.reactivex.Single;

@Dao
public interface AyaDao {

    @Query("SELECT * FROM Aya")
    List<Aya> getAll();

    @Query("SELECT * FROM Aya WHERE id IN (:ayaIds)")
    List<Aya> getAllByIds(int... ayaIds);

    @Query("SELECT * FROM Aya WHERE id=:ayaId")
    Single<Aya> findById(int ayaId);

    @Query("SELECT * FROM Aya WHERE id=:ayaId")
    Aya findAyaById(int ayaId);

    @Query("SELECT * FROM Aya WHERE page=:pageNum")
    Single<List<Aya>> getAllInPage(int pageNum);

    @Query("SELECT * FROM Aya WHERE page=:page AND id=:ayaId LIMIT 1")
    Aya getPageAya(int page, int ayaId);

    @Query("select text, tafseer, pure_text from aya WHERE sura=:suraNumber")
    LiveData<List<TafseerModel>> getPageTafseers(int suraNumber);

    @Query("SELECT * FROM Aya WHERE page=:pageNum LIMIT 1")
    Aya getFirstAyaInPage(int pageNum);

    @Query("SELECT id, sura, pure_text, text, page, sura_aya, juz FROM Aya WHERE id IN (select aya from AyaQuranSubject where subject=:categoryId)")
    Single<List<SearchModel>> getCategoryAyas(int categoryId);

    @Query("SELECT id, sura, pure_text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :input || '%'")
    Single<List<SearchModel>> getSimpleSearchResult(String input);

    @Query("SELECT id, sura, pure_text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :input || '%' and sura=:suraNumber")
    Single<List<SearchModel>> getSuraSearchResult(String input, int suraNumber);

    @Query("SELECT id, sura, pure_text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :input || '%' and juz=:juzNumber")
    Single<List<SearchModel>> getJuzSearchResult(String input, int juzNumber);


    @Query("SELECT distinct sura FROM Aya where juz=:juz ")
    LiveData<List<Integer>> getSurasInChapter(int juz);

    @Query("SELECT id, sura, pure_text, text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :inputSearch || '%' and juz=:selectedJuz and sura=:selectedSura")
    Single<List<SearchModel>> getSuraJuzSearchResult(String inputSearch, int selectedSura, int selectedJuz);


    @Query("SELECT id, sura, pure_text, text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :inputSearch || '%' " +
            "and juz=:selectedJuz and " +
            "id between (select aya_from from hizbquarter where id=:startHezbInterval) AND (select aya_to from hizbquarter where id=:endHezbInterval)")
    Single<List<SearchModel>> getJuzHezbSearchResult(String inputSearch, int selectedJuz, int startHezbInterval, int endHezbInterval);

    @Query("SELECT id, sura, pure_text, text, page, sura_aya, juz FROM Aya WHERE pure_text like '%' || :inputSearch || '%' " +
            "and juz=:selectedJuz and sura=:selectedSura and " +
            "id between (select aya_from from hizbquarter where id=:startHezbInterval) AND (select aya_to from hizbquarter where id=:endHezbInterval)")
    Single<List<SearchModel>> getSuraJuzHezbSearchResult(String inputSearch, int selectedSura, int selectedJuz, int startHezbInterval, int endHezbInterval);

    @Query("select sura, sura_aya, pure_text,text, page from aya where id IN(:ayaIds)")
    Single<List<MyNoteModel>> getNoteData(List<Integer> ayaIds);

    @Query("select DISTINCT (page), sura from aya ")
    Single<List<PageSuras>> getSuraPage();

    @Query("select page from aya where id=:ayaId")
    Single<Integer> getAyaPage(int ayaId);

    @Query("SELECT * FROM Aya where id=(SELECT MIN(id) FROM Aya WHERE sura=:sura)")
    Aya getFirstAyaInSura(int sura);

    @Query("SELECT * FROM Aya where id=(SELECT MAX(id) FROM Aya WHERE sura=:sura)")
    Aya getLastAyaInSura(int sura);

}

