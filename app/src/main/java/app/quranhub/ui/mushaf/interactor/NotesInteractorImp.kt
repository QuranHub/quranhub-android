package app.quranhub.ui.mushaf.interactor

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.Note
import app.quranhub.ui.mushaf.model.DisplayedNote
import app.quranhub.ui.mushaf.model.MyNoteModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers

class NotesInteractorImp(context: Context) : NotesInteractor {

    private val context: Context = context
    private val userDatabase: UserDatabase = UserDatabase.getInstance(context)
    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context.applicationContext)

    override val notes: LiveData<List<DisplayedNote>>
        @SuppressLint("CheckResult")
        get() {
            val notesLivedata = MutableLiveData<List<DisplayedNote>>()
            val noteList = userDatabase.noteDao.getAllNotes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
            val noteDataList = noteList.flatMap { result ->
                val ids = mutableListOf<Int>()
                for (note in result) {
                    ids.add(note.ayaId)
                }
                mushafDatabase.ayaDao.getNoteData(ids).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }
            Single.zip(noteList, noteDataList, BiFunction { notes: List<Note>, myNoteModels: List<MyNoteModel> ->
                val displayedNotes = mutableListOf<DisplayedNote>()
                for (i in notes.indices) {
                    if (i < myNoteModels.size) {
                        displayedNotes.add(
                            DisplayedNote(
                                notes[i].ayaId, notes[i].noteType,
                                notes[i].noteText, notes[i].noteRecorderPath,
                                myNoteModels[i].sura, myNoteModels[i].suraAya,
                                myNoteModels[i].pureText, myNoteModels[i].text, myNoteModels[i].page
                            )
                        )
                    }
                }
                displayedNotes
            }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    notesLivedata.value = result
                }, {
                    Log.d("Error", "Error")
                })
            return notesLivedata
        }

    override fun editNote(note: Note?) {
        if (note != null) {
            Completable.fromAction { userDatabase.noteDao.insertNote(note) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({}, {})
        }
    }

    override fun deleteNote(ayaId: Int) {
        Completable.fromAction { userDatabase.noteDao.deleteNote(ayaId) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({}, {})
    }
}
