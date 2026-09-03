package app.quranhub.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.quranhub.data.local.entity.TranslationBook

@Dao
interface TranslationBookDao {
    @Query("SELECT * FROM TranslationBook")
    fun getAll(): LiveData<List<TranslationBook?>?>?

    @Query("SELECT * FROM TranslationBook WHERE id IN(:ids)")
    fun getAllByIds(vararg ids: Int): LiveData<List<TranslationBook?>?>?

    @Query("SELECT * FROM TranslationBook WHERE language=:langCode")
    fun getByLanguage(langCode: String?): LiveData<List<TranslationBook?>?>?

    @Query("SELECT * FROM TranslationBook WHERE id=:id")
    suspend fun findById(id: String?): TranslationBook?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(translationBook: TranslationBook)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg translationBooks: TranslationBook)

    @Delete
    fun delete(translationBook: TranslationBook)
}