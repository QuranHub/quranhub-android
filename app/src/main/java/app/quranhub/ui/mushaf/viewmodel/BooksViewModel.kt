package app.quranhub.ui.mushaf.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import app.quranhub.data.local.entity.Book;
import app.quranhub.data.remote.model.BookContent;
import app.quranhub.ui.mushaf.interactor.BooksInteractor;
import app.quranhub.ui.mushaf.interactor.BooksInteractorImp;

public class BooksViewModel extends AndroidViewModel implements BooksInteractor.TranslationsListener {

    private BooksInteractor booksInteractor;
    private LiveData<List<Book>> result;
    private MediatorLiveData<List<Book>> localTranslationsLiveData;
    private MediatorLiveData<List<BookContent>> remoteTranslationsLiveData;

    public BooksViewModel(@NonNull Application application) {
        super(application);
        booksInteractor = new BooksInteractorImp(application.getApplicationContext(), this);
        localTranslationsLiveData = new MediatorLiveData<>();
        remoteTranslationsLiveData = new MediatorLiveData<>();
        booksInteractor.getAllTranslations();

        result = booksInteractor.getLocallyTranslations();
        localTranslationsLiveData.addSource(result, translationModels -> {
            localTranslationsLiveData.setValue(translationModels);
        });

    }

    @Override
    public void onError() {
        localTranslationsLiveData.setValue(null);
    }

    @Override
    public void onGetAllTranslation(List<BookContent> contents) {
        remoteTranslationsLiveData.setValue(contents);
    }

    public void updateTranslationType(int id, int type, long downloadId) {
        booksInteractor.updateTranslationDownload(id, type, downloadId);
    }

    public void updateFinishedDownload(long downloadId, int type) {
        booksInteractor.updateFinishedDownload(downloadId, type);

    }

    public LiveData<List<Book>> getLocalTranslationsLiveData() {
        return localTranslationsLiveData;
    }

    public MediatorLiveData<List<BookContent>> getRemoteTranslationsLiveData() {
        return remoteTranslationsLiveData;
    }
}
