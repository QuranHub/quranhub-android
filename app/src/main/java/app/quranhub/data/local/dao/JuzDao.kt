package app.quranhub.data.local.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.Juz;

@Dao
public interface JuzDao {

    @Query("SELECT * FROM Juz")
    List<Juz> getAll();

    @Query("SELECT * FROM Juz WHERE id=:id")
    Juz getById(int id);

}
