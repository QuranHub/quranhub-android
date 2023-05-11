package app.quranhub.data.local.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.Recitation;

@Dao
public interface RecitationDao {

    @Query("SELECT * FROM Recitation")
    List<Recitation> getAll();

    @Query("SELECT * FROM Recitation where id IN (:recitationsIds)")
    List<Recitation> getAllByIds(int[] recitationsIds);

    @Query("SELECT * FROM Recitation Where id=:recitationId")
    Recitation getById(int recitationId);

    @Insert
    void insert(Recitation recitation);

    @Insert
    void insertAll(Recitation[] recitations);

    @Delete
    void delete(Recitation recitation);

}
