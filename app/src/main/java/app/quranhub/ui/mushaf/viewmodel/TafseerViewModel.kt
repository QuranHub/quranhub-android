package app.quranhub.ui.mushaf.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import app.quranhub.data.local.entity.Translation;
import app.quranhub.ui.mushaf.interactor.TafseerInteractor;
import app.quranhub.ui.mushaf.interactor.TafseerInteractorImp;
import app.quranhub.ui.mushaf.model.TafseerModel;

public class TafseerViewModel extends AndroidViewModel {

    private TafseerInteractor interactor;
    private MediatorLiveData<List<TafseerModel>> tafseers;
    private MediatorLiveData<List<Translation>> bookTafseers;
    private MediatorLiveData<List<TafseerModel>> ayahs;
    private LiveData<List<TafseerModel>> ayasLiveData;
    private LiveData<List<Translation>> bookTafseersLiveData;
    private LiveData<List<TafseerModel>> tafseerLiveData;

    public TafseerViewModel(@NonNull Application application) {
        super(application);
        interactor = new TafseerInteractorImp(application);
        tafseers = new MediatorLiveData<>();
        bookTafseers = new MediatorLiveData<>();
        ayahs = new MediatorLiveData<>();
    }

    public void getSuraTafseers(int suraNumber) {
        tafseerLiveData = interactor.getSuraTafseers(suraNumber);
        tafseers.addSource(tafseerLiveData, tafseerModels -> {
            tafseers.setValue(tafseerModels);
            tafseers.removeSource(tafseerLiveData);
        });
    }

    public MediatorLiveData<List<TafseerModel>> getTafseers() {
        return tafseers;
    }

    public MediatorLiveData<List<Translation>> getBookTafseers() {
        return bookTafseers;
    }

    public MediatorLiveData<List<TafseerModel>> getAyahs() {
        return ayahs;
    }


    public void getSuraTafseers(String bookDbName, int suraNumber) {
        interactor.initTranslationDB(bookDbName);
        bookTafseersLiveData = interactor.getSuraBookTafseers(suraNumber);
        bookTafseers.addSource(bookTafseersLiveData, tafseerModels -> {
            bookTafseers.setValue(tafseerModels);
            bookTafseers.removeSource(bookTafseersLiveData);
        });
    }

    public void getSuraAyahs(int suraNumber) {
        ayasLiveData = interactor.getSuraTafseers(suraNumber);
        ayahs.addSource(ayasLiveData, tafseerModels -> {
            ayahs.setValue(tafseerModels);
            ayahs.removeSource(ayasLiveData);
        });
    }
}
