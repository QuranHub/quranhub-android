package app.quranhub.ui.mushaf.interactor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.R;
import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.entity.Aya;
import app.quranhub.data.local.entity.AyaBookmark;
import app.quranhub.data.local.entity.BookmarkType;
import app.quranhub.data.local.entity.Note;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class QuranPageInteractorImp implements QuranPageInteractor {

    private static final String TAG = QuranPageInteractorImp.class.getSimpleName();

    private MushafDatabase mushafDatabase;
    private UserDatabase userDatabase;

    private QuranPageInteractor.ResultListener resultListener;
    private Context context;
    private int recitationId;
    private String sheikhId;

    public QuranPageInteractorImp(Context context, ResultListener resultListener) {
        mushafDatabase = MushafDatabase.getInstance(context);
        userDatabase = UserDatabase.getInstance(context);
        this.resultListener = resultListener;
        this.context = context;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void getPageAyaWithPrevious(int pageNumber, int ayaId) {
        // TODO refactor AsyncTask
        new AsyncTask<Integer, Void, List<Aya>>() {
            @Override
            protected List<Aya> doInBackground(Integer... integers) {
                List<Aya> ayas = new ArrayList<>();
                if (integers.length == 2) {
                    int pageNumber = integers[0];
                    int ayaId = integers[1];
                    ayas.add(mushafDatabase.getAyaDao().getPageAya(pageNumber, ayaId)); //  aya
                    ayas.add(mushafDatabase.getAyaDao().getPageAya(pageNumber, ayaId - 1));  //  its previous aya
                }
                return ayas;
            }

            @Override
            protected void onPostExecute(List<Aya> pageAyas) {
                Log.d(TAG, "getPageAyaWithPrevious::onPostExecute - pageAyas.size()=" + pageAyas.size());
                if (pageAyas.size() == 2) {
                    resultListener.onGetAyaWithPrevious(pageAyas.get(0), pageAyas.get(1));
                }
            }
        }.execute(pageNumber, ayaId);
    }

    @SuppressLint("CheckResult")
    @Override
    public void getPageAyas(int page) {
        mushafDatabase.getAyaDao().getAllInPage(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result != null) {
                        resultListener.onGetPageAyas(result);
                    } else {
                        resultListener.showMessage(context.getString(R.string.page_info_failed));
                    }
                }, error -> {
                    Log.d(TAG, "getPageAyas: " + error.toString());
                    resultListener.showMessage(context.getString(R.string.page_info_failed));
                });

    }

    @SuppressLint("CheckResult")
    @Override
    public void getBookmarkType(int ayaId) {
        userDatabase.getBookmarkDao().getBookmarkType(ayaId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(type -> {
                    if (type != null) {
                        resultListener.onGetBookmarkType(type);
                    } else {
                        Log.d(TAG, "getBookmarkType: No type");
                    }
                }, error -> {
                    Log.d(TAG, "getBookmarkType: Error");
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void insertAyaBookmark(AyaBookmark ayaBookmark) {
        Completable.fromAction(() ->
                        userDatabase.getBookmarkDao().insertAyaBookmark(ayaBookmark))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        resultListener.showMessage(context.getString(R.string.success_insert_bookmark));
                    }

                    @Override
                    public void onError(Throwable e) {
                        resultListener.showMessage(context.getString(R.string.insert_bookmark_failed));
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void removeBookmark(int ayaId) {


        Completable.fromAction(() ->
                        userDatabase.getBookmarkDao().deleteAyaBookmark(ayaId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        resultListener.onSuccessRemoveBookmark();
                    }

                    @Override
                    public void onError(Throwable e) {
                        resultListener.showMessage(context.getString(R.string.bookmark_failed_removed));
                    }
                });
    }

    @Override
    public void addNote(Note note) {
        Completable.fromAction(() ->
                        userDatabase.getNoteDao().insertNote(note))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        resultListener.onSuccessAddNote();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ");
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void checkAyaNote(int ayaId) {
        userDatabase.getNoteDao().getAyaNote(ayaId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    resultListener.onGetAyaNote(result);
                }, error -> {

                });

    }

    @SuppressLint("CheckResult")
    @Override
    public void getBookmarkTypes() {
        userDatabase.getBookmarkDao().getBookmarkTypes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    resultListener.onGetBookmarkTypes(result);
                }, error -> {
                    Log.e(TAG, "onError: getBookmarkTypes");
                });
    }

    @Override
    public void insertCustomBookmark(Aya currentAya, BookmarkType type) {

        Completable.fromAction(() ->
                        userDatabase.getBookmarkDao().insertBookmarkType(type))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        insertAyaBookmark(new AyaBookmark(currentAya.getId(), type.getTypeId(), currentAya));
                    }

                    @Override
                    public void onError(Throwable e) {
                        resultListener.showMessage(context.getString(R.string.insert_bookmark_failed));
                    }
                });

    }


}
