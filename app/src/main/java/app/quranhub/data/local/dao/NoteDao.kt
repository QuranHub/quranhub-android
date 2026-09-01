package app.quranhub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.quranhub.data.local.entity.Note

@Dao
interface NoteDao {
    @Query("select * from note")
    suspend fun getAllNotes(): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: Note)

    @Query("select * from note where ayaId=:ayaId")
    suspend fun getAyaNote(ayaId: Int): Note?

    @Query("delete from note where ayaId=:ayaId")
    fun deleteNote(ayaId: Int)
}