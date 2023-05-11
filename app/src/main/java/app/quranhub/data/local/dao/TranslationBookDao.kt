package app.quranhub.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.TranslationBook;
import io.reactivex.Single;

@Dao
public interface TranslationBookDao {

    @Query("SELECT * FROM TranslationBook")
    LiveData<List<TranslationBook>> getAll();

    @Query("SELECT * FROM TranslationBook WHERE id IN(:ids)")
    LiveData<List<TranslationBook>> getAllByIds(int... ids);

    @Query("SELECT * FROM TranslationBook WHERE language=:langCode")
    LiveData<List<TranslationBook>> getByLanguage(String langCode);

    @Query("SELECT * FROM TranslationBook WHERE id=:id")
    Single<TranslationBook> findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TranslationBook translationBook);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(TranslationBook... translationBooks);

    @Delete
    void delete(TranslationBook translationBook);

}
