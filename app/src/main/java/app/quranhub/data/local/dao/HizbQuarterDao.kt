package app.quranhub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.HizbQuarter;
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel;
import io.reactivex.Single;

@Dao
public interface HizbQuarterDao {

    @Query("SELECT * FROM HizbQuarter")
    Single<List<HizbQuarter>> getAll();

    @Query("SELECT * FROM HizbQuarter WHERE id=:id")
    HizbQuarter getById(int id);

    @Query("SELECT s.id AS sura_number, a1.sura_aya AS aya_number, a1.pure_text AS aya_text\n" +
            ", a1.page AS start_page, a2.page AS end_page, a1.juz\n" +
            ", CAST((h.id-1)/4 AS INTEGER)%2+1 AS hizb, ((h.id-1)%4)+1 AS quarter\n" +
            "FROM Aya a1 INNER JOIN HizbQuarter h ON a1.id = h.aya_from\n" +
            "INNER JOIN Aya a2 ON a2.id = h.aya_to\n" +
            "INNER JOIN Sura s ON s.id = a1.sura\n" +
            "WHERE h.id = :hizbQuarterId;")
    HizbQuarterDataModel getHizbQuarterDataModelById(int hizbQuarterId);

    @Query("SELECT s.id AS sura_number, a1.sura_aya AS aya_number, a1.pure_text AS aya_text\n" +
            ", a1.page AS start_page, a2.page AS end_page, a1.juz\n" +
            ", CAST((h.id-1)/4 AS INTEGER)%2+1 AS hizb, ((h.id-1)%4)+1 AS quarter\n" +
            "FROM Aya a1 INNER JOIN HizbQuarter h ON a1.id = h.aya_from\n" +
            "INNER JOIN Aya a2 ON a2.id = h.aya_to\n" +
            "INNER JOIN Sura s ON s.id = a1.sura\n" +
            "WHERE :ayaId BETWEEN h.aya_from AND h.aya_to ;")
    HizbQuarterDataModel getHizbQuarterDataModelForAya(int ayaId);

    @Query("SELECT s.id AS sura_number, a1.sura_aya AS aya_number, a1.pure_text AS aya_text\n" +
            ", a1.page AS start_page, a2.page AS end_page, a1.juz\n" +
            ", CAST((h.id-1)/4 AS INTEGER)%2+1 AS hizb, ((h.id-1)%4)+1 AS quarter\n" +
            "FROM Aya a1 INNER JOIN HizbQuarter h ON a1.id = h.aya_from\n" +
            "INNER JOIN Aya a2 ON a2.id = h.aya_to\n" +
            "INNER JOIN Sura s ON s.id = a1.sura;")
    LiveData<List<HizbQuarterDataModel>> getAllHizbQuarterDataModel();

}
