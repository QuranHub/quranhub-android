package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import app.quranhub.data.local.entity.HizbQuarter
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow

@Dao
interface HizbQuarterDao {
    @Query("SELECT * FROM HizbQuarter")
    fun getAll(): Single<List<HizbQuarter>>

    @Query("SELECT * FROM HizbQuarter WHERE id=:id")
    fun getById(id: Int): HizbQuarter?

    @Query(
        """
            SELECT s.id AS sura_number, a1.sura_aya AS aya_number, a1.pure_text AS aya_text
            , a1.page AS start_page, a2.page AS end_page, a1.juz
            , CAST((h.id-1)/4 AS INTEGER)%2+1 AS hizb, ((h.id-1)%4)+1 AS quarter
            FROM Aya a1 INNER JOIN HizbQuarter h ON a1.id = h.aya_from
            INNER JOIN Aya a2 ON a2.id = h.aya_to
            INNER JOIN Sura s ON s.id = a1.sura
            WHERE h.id = :hizbQuarterId;
        """
    )
    fun getHizbQuarterDataModelById(hizbQuarterId: Int): HizbQuarterDataModel?

    @Query(
        """
            SELECT s.id AS sura_number, a1.sura_aya AS aya_number, a1.pure_text AS aya_text
            , a1.page AS start_page, a2.page AS end_page, a1.juz
            , CAST((h.id-1)/4 AS INTEGER)%2+1 AS hizb, ((h.id-1)%4)+1 AS quarter
            FROM Aya a1 INNER JOIN HizbQuarter h ON a1.id = h.aya_from
            INNER JOIN Aya a2 ON a2.id = h.aya_to
            INNER JOIN Sura s ON s.id = a1.sura
            WHERE :ayaId BETWEEN h.aya_from AND h.aya_to ;
        """
    )
    fun getHizbQuarterDataModelForAya(ayaId: Int): HizbQuarterDataModel?

    @Query(
        """
            SELECT s.id AS sura_number, a1.sura_aya AS aya_number, a1.pure_text AS aya_text
            , a1.page AS start_page, a2.page AS end_page, a1.juz
            , CAST((h.id-1)/4 AS INTEGER)%2+1 AS hizb, ((h.id-1)%4)+1 AS quarter
            FROM Aya a1 INNER JOIN HizbQuarter h ON a1.id = h.aya_from
            INNER JOIN Aya a2 ON a2.id = h.aya_to
            INNER JOIN Sura s ON s.id = a1.sura;
        """
    )
    fun getAllHizbQuarterDataModel(): Flow<List<HizbQuarterDataModel>>
}