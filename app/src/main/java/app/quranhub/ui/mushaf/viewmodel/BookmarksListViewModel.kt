package app.quranhub.ui.mushaf.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.data.local.dao.HizbQuarterDao;
import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.entity.BookmarkType;
import app.quranhub.ui.mushaf.interactor.BookmarksInteractor;
import app.quranhub.ui.mushaf.interactor.BookmarksInteractorImp;
import app.quranhub.ui.mushaf.model.DisplayableBookmark;
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel;

public class BookmarksListViewModel extends AndroidViewModel {

    @NonNull
    private BookmarksInteractor bookmarksInteractor;

    @NonNull
    private MediatorLiveData<List<DisplayableBookmark>> bookmarks;
    private LiveData<List<DisplayableBookmark>> bookmarkLiveData;
    private MediatorLiveData<List<BookmarkType>> bookmarksTypes;
    private LiveData<List<BookmarkType>> bookmarkTypeLiveData;
    private Context context;

    public BookmarksListViewModel(@NonNull Application application) {
        super(application);
        context = application;
        bookmarksInteractor = new BookmarksInteractorImp(application.getApplicationContext());
        bookmarkLiveData = bookmarksInteractor.getAllBookmarks();
        bookmarksTypes = new MediatorLiveData<>();
        bookmarks = new MediatorLiveData<>();
        bookmarks.addSource(bookmarkLiveData, ayaBookmarks -> {
            bookmarks.setValue(ayaBookmarks);
        });
    }


    @NonNull
    public LiveData<List<DisplayableBookmark>> getBookmarks() {
        return bookmarks;
    }


    @NonNull
    public LiveData<List<BookmarkType>> getBookmarksTypes() {
        return bookmarksTypes;
    }

    /**
     * Maps a List of AyaBookmark to a List of DisplayableBookmark.
     *
     * @param ayaBookmarks
     * @param listener
     */
    @SuppressLint("StaticFieldLeak")
    public void bookmarksMapper(@NonNull List<DisplayableBookmark> ayaBookmarks
            , @NonNull BookmarkMapperListener listener) {

        // TODO refactor AsyncTask
        new AsyncTask<DisplayableBookmark, Void, List<DisplayableBookmark>>() {
            @Override
            protected List<DisplayableBookmark> doInBackground(DisplayableBookmark... bookmarks) {
                // TODO simplify the DB queries
                HizbQuarterDao dao = MushafDatabase
                        .getInstance(context)
                        .getHizbQuarterDao();

                List<DisplayableBookmark> result = new ArrayList<>();
                for (DisplayableBookmark bookmark : bookmarks) {
                    HizbQuarterDataModel hizbQuarterDataModel = dao.getHizbQuarterDataModelForAya(bookmark.getAyaId());

                    bookmark.setHizbNumber(hizbQuarterDataModel.getHizb());
                    bookmark.setRub3Number(hizbQuarterDataModel.getQuarter());

                    result.add(bookmark);
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<DisplayableBookmark> displayableBookmarks) {
                listener.onDisplayableBookmarksReady(displayableBookmarks);
            }
        }.execute(ayaBookmarks.toArray(new DisplayableBookmark[0]));

    }

    public void deleteBookmark(int bookmarkId) {
        bookmarksInteractor.deleteBookmark(bookmarkId);
    }

    public void changeBookmarkType(int bookmarkId, int bookmarkTypeId) {
        bookmarksInteractor.changeBookmarkType(bookmarkId, bookmarkTypeId);
    }

    public void getBookmarkTypes() {
        bookmarkTypeLiveData = bookmarksInteractor.getBookmarkTypes();
        bookmarksTypes.addSource(bookmarkTypeLiveData, types -> {
            bookmarksTypes.setValue(types);
        });
    }


    public interface BookmarkMapperListener {
        void onDisplayableBookmarksReady(List<DisplayableBookmark> displayableBookmarks);
    }

}
