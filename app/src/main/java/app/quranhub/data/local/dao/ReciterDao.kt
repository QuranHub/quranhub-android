package app.quranhub.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.Reciter;

@Dao
public interface ReciterDao {

    @Query("SELECT * FROM Reciter")
    List<Reciter> getAll();

    @Query("SELECT * FROM Reciter where id IN (:recitersIds)")
    List<Reciter> getAllByIds(int[] recitersIds);

    @Query("SELECT s.id, s.name, s.nationality, s.audio_base_url FROM Reciter as s JOIN ReciterRecitation as sr " +
            "ON s.id=sr.reciter_id WHERE sr.recitation_id=:recitationId")
    List<Reciter> getAllForRecitation(int recitationId);

    @Query("SELECT * FROM Reciter Where id=:reciterId")
    Reciter getById(String reciterId);

    @Insert
    void insert(Reciter reciter);

    @Insert
    void insertAll(Reciter[] reciters);

    @Delete
    void delete(Reciter reciter);

    @Delete
    void deleteAll(Reciter[] reciters);

}
