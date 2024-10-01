package app.quranhub.ui.mushaf.interactor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.entity.AyaBookmark;
import app.quranhub.data.local.entity.BookmarkType;
import app.quranhub.data.local.entity.Sura;
import app.quranhub.ui.mushaf.model.DisplayableBookmark;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BookmarksInteractorImp implements BookmarksInteractor {

    private static final String TAG = BookmarksInteractorImp.class.getSimpleName();

    @NonNull
    private final UserDatabase userDatabase;
    @NonNull
    private final MushafDatabase mushafDatabase;

    @NonNull
    private final Context context;

    public BookmarksInteractorImp(@NonNull Context context) {
        this.context = context;
        userDatabase = UserDatabase.getInstance(context.getApplicationContext());
        mushafDatabase = MushafDatabase.getInstance(context.getApplicationContext());
    }

    @Override
    public Sura getSura(int suraId) {
        return mushafDatabase.getSuraDao().findById(suraId);
    }

    @SuppressLint("CheckResult")
    @Override
    public LiveData<List<DisplayableBookmark>> getAllBookmarks() {

        MutableLiveData<List<DisplayableBookmark>> listLiveData = new MutableLiveData<>();


        Single<List<AyaBookmark>> bookmarks = userDatabase.getBookmarkDao().getAllBookmarks().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        Single<List<BookmarkType>> bookmarkTypes = userDatabase.getBookmarkDao().getBookmarkTypes().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        Single.zip(bookmarks, bookmarkTypes, (ayaBookmarks, types) -> {
                    List<DisplayableBookmark> displayableBookmarks = new ArrayList<>();
                    for (int i = 0; i < ayaBookmarks.size(); i++) {
                        DisplayableBookmark displayableBookmark = new DisplayableBookmark();
                        String suraName = context.getResources().getStringArray(R.array.sura_name)[ayaBookmarks.get(i).getAya().getSura() - 1];
                        displayableBookmark.setSuraName(suraName);
                        displayableBookmark.setBookmarkId(ayaBookmarks.get(i).getBookmarkId());
                        displayableBookmark.setBookmarkType(ayaBookmarks.get(i).getBookmarkTypeId());
                        displayableBookmark.setAyaContent(ayaBookmarks.get(i).getAya().getPureText());
                        displayableBookmark.setAyaId(ayaBookmarks.get(i).getAya().getId());
                        displayableBookmark.setSuraAyaNumber(ayaBookmarks.get(i).getAya().getSuraAya());
                        displayableBookmark.setGuz2Number(ayaBookmarks.get(i).getAya().getJuz());
                        displayableBookmark.setPageNumber(ayaBookmarks.get(i).getAya().getPage());

                        for (int j = 0; j < types.size(); j++) {
                            if (types.get(j).getTypeId() == ayaBookmarks.get(i).getBookmarkTypeId()) {
                                displayableBookmark.setColorIndex(types.get(j).getColorIndex());
                                break;
                            }
                        }
                        displayableBookmarks.add(displayableBookmark);
                    }
                    return displayableBookmarks;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    listLiveData.setValue(result);
                }, error -> {
                    Log.d("Error", "Error");
                });

        return listLiveData;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void deleteBookmark(int bookmarkId) {
        // TODO refactor AsyncTask
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... integers) {
                if (integers.length == 1) {
                    userDatabase.getBookmarkDao().deleteAyaBookmark(integers[0]);
                }
                return null;
            }
        }.execute(bookmarkId);
    }

    @Override
    public LiveData<List<AyaBookmark>> filterBookmarks(int filterType) {
        return userDatabase.getBookmarkDao().getFilterBookmaks(filterType);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void changeBookmarkType(int bookmarkId, int bookmarkTypeId) {
        // TODO refactor AsyncTask
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... integers) {
                if (integers.length == 2) {
                    int bookmarkId = integers[0];
                    int bookmarkTypeId = integers[1];

                    userDatabase.getBookmarkDao().changeAyaBookmarkType(bookmarkId, bookmarkTypeId);
                }
                return null;
            }
        }.execute(bookmarkId, bookmarkTypeId);
    }

    @Override
    public LiveData<List<BookmarkType>> getBookmarkTypes() {
        return userDatabase.getBookmarkDao().getBookmarkTypesLiveData();
    }

}
