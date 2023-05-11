package app.quranhub.data.local.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.Sura;
import app.quranhub.ui.mushaf.model.QuranPageInfo;
import app.quranhub.ui.mushaf.model.SuraIndexModel;
import app.quranhub.ui.mushaf.model.SuraVersesNumber;
import io.reactivex.Single;

@Dao
public interface SuraDao {

    @Query("SELECT * FROM Sura")
    Single<List<Sura>> getAll();

    @Query("SELECT * FROM Sura WHERE id=:suraId")
    Sura findById(int suraId);

    @Query("SELECT juz, sura from aya where page=:currentPage LIMIT 1")
    Single<QuranPageInfo> getQuranPageInfo(int currentPage);

    @Query("select sura.id, sura.ayas, sura.type, aya.juz, aya.page, aya.sura from sura join aya on aya.sura=sura.id and aya.sura_aya=1")
    Single<List<SuraIndexModel>> getSuraIndexInfo();

    @Query("select id, ayas from sura")
    Single<List<SuraVersesNumber>> getSuraVersesNumber();
}
