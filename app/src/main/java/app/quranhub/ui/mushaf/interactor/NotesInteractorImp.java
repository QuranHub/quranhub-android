package app.quranhub.ui.mushaf.interactor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.entity.Note;
import app.quranhub.ui.mushaf.model.DisplayedNote;
import app.quranhub.ui.mushaf.model.MyNoteModel;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class NotesInteractorImp implements NotesInteractor {

    private Context context;
    @NonNull
    private UserDatabase userDatabase;
    private MushafDatabase mushafDatabase;


    public NotesInteractorImp(@NonNull Context context) {
        this.context = context;
        userDatabase = UserDatabase.getInstance(context);
        mushafDatabase = MushafDatabase.getInstance(context.getApplicationContext());
    }

    @SuppressLint("CheckResult")
    @Override
    public LiveData<List<DisplayedNote>> getNotes() {

        MutableLiveData<List<DisplayedNote>> notesLivedata = new MutableLiveData<>();

        Single<List<Note>> noteList = userDatabase.getNoteDao().getAllNotes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());


        Single<List<MyNoteModel>> noteDataList = noteList.flatMap(result -> {
            List<Integer> ids = new ArrayList<>();
            for (Note note : result) {
                ids.add(note.getAyaId());
            }
            return mushafDatabase.getAyaDao().getNoteData(ids).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        });

        Single.zip(noteList, noteDataList, (notes, myNoteModels) -> {
                    List<DisplayedNote> displayedNotes = new ArrayList<>();
                    for (int i = 0; i < notes.size(); i++) {
                        if (i < myNoteModels.size()) {
                            displayedNotes.add(new DisplayedNote(notes.get(i).getAyaId(), notes.get(i).getNoteType(),
                                    notes.get(i).getNoteText(), notes.get(i).getNoteRecorderPath()
                                    , myNoteModels.get(i).getSura(), myNoteModels.get(i).getSuraAya(),
                                    myNoteModels.get(i).getPureText(), myNoteModels.get(i).getText(), myNoteModels.get(i).getPage()));
                        }
                    }
                    return displayedNotes;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    notesLivedata.setValue(result);
                }, error -> {
                    Log.d("Error", "Error");
                });

        return notesLivedata;
    }


    @Override
    public void editNote(Note note) {
        Completable.fromAction(() ->
                        userDatabase.getNoteDao().insertNote(note))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void deleteNote(int ayaId) {
        Completable.fromAction(() ->
                        userDatabase.getNoteDao().deleteNote(ayaId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

}


