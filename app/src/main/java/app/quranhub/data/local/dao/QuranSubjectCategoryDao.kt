package app.quranhub.data.local.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.QuranSubjectCategory;
import io.reactivex.Single;

@Dao
public interface QuranSubjectCategoryDao {

    @Query("SELECT * FROM QuranSubjectCategory")
    Single<List<QuranSubjectCategory>> getAll();

    @Query("SELECT * FROM QuranSubjectCategory WHERE id IN(:ids)")
    List<QuranSubjectCategory> getAllByIds(int... ids);


}
