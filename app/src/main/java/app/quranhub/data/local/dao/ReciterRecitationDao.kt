package app.quranhub.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.Reciter;
import app.quranhub.data.local.entity.ReciterRecitation;

@Dao
public interface ReciterRecitationDao {

    @Query("SELECT * FROM ReciterRecitation")
    List<ReciterRecitation> getAll();

    @Query("SELECT * FROM ReciterRecitation where id IN (:sheikhRecitationsIds)")
    List<ReciterRecitation> getAllByIds(int[] sheikhRecitationsIds);

    @Query("SELECT * FROM ReciterRecitation Where id=:sheikhRecitationId")
    ReciterRecitation getById(int sheikhRecitationId);

    @Query("SELECT * FROM ReciterRecitation WHERE recitation_id=:recitationId AND reciter_id=:reciterId")
    ReciterRecitation get(int recitationId, String reciterId);

    @Query("SELECT id FROM ReciterRecitation WHERE recitation_id=:recitationId AND reciter_id=:sheikhId")
    int getSheikhRecitationId(int recitationId, String sheikhId);

    @Query("SELECT COUNT(DISTINCT sr.reciter_id) FROM ReciterRecitation as sr JOIN QuranAudio as q " +
            "ON sr.id=q.sheikh_recitation_id WHERE sr.recitation_id=:recitationId")
    int getNumOfRecitersWithDownloads(int recitationId);

    @Query("SELECT s.* FROM Reciter as s JOIN ReciterRecitation as sr ON s.id=sr.reciter_id WHERE recitation_id=:recitationId")
    List<Reciter> getRecitersForRecitation(int recitationId);

    @Query("SELECT DISTINCT sura FROM Reciter as s JOIN ReciterRecitation as sr JOIN QuranAudio as q " +
            "ON s.id=sr.reciter_id AND sr.id=q.sheikh_recitation_id WHERE sr.recitation_id=:recitationId " +
            "AND s.id=:reciterId")
    List<Integer> getSurasIdsForReciterInRecitation(int recitationId, String reciterId);

    @Insert
    void insert(ReciterRecitation reciterRecitation);

    @Insert
    void insertAll(ReciterRecitation[] reciterRecitations);

    @Delete
    void delete(ReciterRecitation reciterRecitation);

    @Query("DELETE FROM ReciterRecitation WHERE recitation_id=:recitationId AND reciter_id=:reciterId")
    void delete(int recitationId, String reciterId);

}
