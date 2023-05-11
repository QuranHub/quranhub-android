package app.quranhub.ui.mushaf.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import java.util.List;

import app.quranhub.ui.mushaf.interactor.SearchInteractor;
import app.quranhub.ui.mushaf.interactor.SearchInteractorImp;
import app.quranhub.ui.mushaf.model.SearchModel;

public class SearchViewModel extends AndroidViewModel implements SearchInteractor.TopicListener {

    private SearchInteractor interactor;
    private LiveData<List<Integer>> suraLiveData;
    private MediatorLiveData<List<Integer>> sura;
    private MediatorLiveData<List<SearchModel>> search;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        interactor = new SearchInteractorImp(application, this);
        sura = new MediatorLiveData<>();
        search = new MediatorLiveData<>();
    }

    public void simpleSearch(String input) {
        interactor.searchAya(input);
    }

    public void searchWithSura(String input, int suraNumber) {
        interactor.searchAyaInSura(input, suraNumber);
    }

    public void searchWithJuz(String input, int juzNumber) {
        interactor.searchAyaInGuz(input, juzNumber);
    }

    public void getChapterSuras(int juzNumber) {
        suraLiveData = interactor.getSurasInChapter(juzNumber);
        sura.addSource(suraLiveData, tafseerModels -> {
            sura.setValue(tafseerModels);
            sura.removeSource(suraLiveData);
        });
    }

    public void searchWithSuraAndJuz(String inputSearch, int selectedSura, int selectedJuz) {
        interactor.searchWithSuraAndJuz(inputSearch, selectedSura, selectedJuz);
    }

    public void searchWithSuraAndJuzAndHizbQuarter(String inputSearch, int selectedSura, int selectedJuz, int selectedHezb, int selectedQuarter) {
        interactor.searchWithSuraAndJuzAndHizbQuarter(inputSearch, selectedSura, selectedJuz, selectedHezb, selectedQuarter);
    }

    public void searchWithSuraAndJuzAndHizb(String inputSearch, int selectedSura, int selectedJuz, int selectedHezb) {
        interactor.searchWithSuraAndJuzAndHizb(inputSearch, selectedSura, selectedJuz, selectedHezb);
    }


    public MediatorLiveData<List<Integer>> getSura() {
        return sura;
    }

    public MediatorLiveData<List<SearchModel>> getSearch() {
        return search;
    }


    @Override
    public void onGetTopics(List<SearchModel> searchModels) {
        search.setValue(searchModels);
    }
}
