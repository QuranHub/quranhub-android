package app.quranhub.data.local.dao;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import app.quranhub.data.local.entity.Note;
import io.reactivex.Single;


@Dao
public interface NoteDao {

    @Query("select * from note")
    Single<List<Note>> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Query("select * from note where ayaId=:ayaId")
    Single<Note> getAyaNote(int ayaId);

    @Query("delete from note where ayaId=:ayaId")
    void deleteNote(int ayaId);
}
