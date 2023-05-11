package app.quranhub.data.local.dao;

import androidx.room.Dao;
import androidx.room.Query;

import app.quranhub.data.local.entity.AyaQuranSubject;

@Dao
public interface AyaQuranSubjectDao {

    @Query("SELECT * FROM AyaQuranSubject WHERE id=:id")
    AyaQuranSubject findById(int id);

}
