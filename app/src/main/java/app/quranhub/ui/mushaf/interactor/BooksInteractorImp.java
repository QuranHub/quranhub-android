package app.quranhub.ui.mushaf.interactor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.db.UserDatabase;
import app.quranhub.data.local.entity.Book;
import app.quranhub.data.remote.ApiClient;
import app.quranhub.data.remote.api.BooksApi;
import app.quranhub.data.remote.model.BookContent;
import app.quranhub.ui.mushaf.model.TafseerModel;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class BooksInteractorImp implements BooksInteractor {

    private static final String TAG = BooksInteractorImp.class.getSimpleName();


    private Context context;
    private UserDatabase userDatabase;
    private BooksInteractor.TranslationsListener listener;
    private BooksApi booksApi;
    @NonNull
    private MushafDatabase mushafDatabase;


    public BooksInteractorImp(Context context, BooksInteractor.TranslationsListener listener) {
        this.context = context;
        userDatabase = UserDatabase.getInstance(context);
        booksApi = ApiClient.getClient().create(BooksApi.class);
        mushafDatabase = MushafDatabase.getInstance(context.getApplicationContext());
        this.listener = listener;
    }

    @SuppressLint("CheckResult")
    @Override
    public void getAllTranslations() {
        booksApi.getAllBooks()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    saveTranslationsLocally(res.getBooks());
                    listener.onGetAllTranslation(res.getBooks());
                }, error -> {
                    listener.onError();
                });
    }

    @Override
    public LiveData<List<Book>> getLocallyTranslations() {
        return userDatabase.getBookDao().getAllTranslations();
    }

    @Override
    public LiveData<List<TafseerModel>> getSuraTafseers(int suraNumber) {
        return mushafDatabase.getAyaDao().getPageTafseers(suraNumber);

    }

    // save books locally to save its status (downloaded, not-downloaded, in-progress downloading) to change each book UI according to download status
    private void saveTranslationsLocally(List<BookContent> bookList) {
        List<Book> results = new ArrayList<>();
        for (BookContent content : bookList) {
            results.add(new Book(content));
        }
        Completable.fromAction(() ->
                        userDatabase.getBookDao().insertDownloadedTranslation(results))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: ");
                    }
                });


    }

    @Override
    public void updateTranslationDownload(int id, int type, long downloadId) {
        Completable.fromAction(() ->
                        userDatabase.getBookDao().updateDownlodedTranslation(id, type, downloadId))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        Log.d("Ss5", "onComplete: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }
                });
    }

    @Override
    public void updateFinishedDownload(long downloadId, int type) {
        Completable.fromAction(() ->
                        userDatabase.getBookDao().updateFinishedDownload(downloadId, type))
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
