package app.quranhub.ui.mushaf.interactor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;

import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.entity.HizbQuarter;
import app.quranhub.ui.mushaf.model.SearchModel;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SearchInteractorImp implements SearchInteractor {

    private Context context;
    @NonNull
    private MushafDatabase mushafDatabase;
    private SearchInteractor.TopicListener listener;

    public SearchInteractorImp(@NonNull Context context, SearchInteractor.TopicListener listener) {
        this.context = context;
        mushafDatabase = MushafDatabase.getInstance(context.getApplicationContext());
        this.listener = listener;
    }

    @Override
    public void searchAya(String inputQuery) {
        Single<List<SearchModel>> searchModels = mushafDatabase.getAyaDao()
                .getSimpleSearchResult(inputQuery)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        getData(searchModels);
    }

    @SuppressLint("CheckResult")
    private void getData(Single<List<SearchModel>> searchModels) {

        Single<List<HizbQuarter>> hizbQuarterData = mushafDatabase.getHizbQuarterDao().getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Single.zip(searchModels, hizbQuarterData, (searchModels1, hizbQuarters) -> {
                    int[] ayaHezbQuarterIndex = new int[6237];
                    for (HizbQuarter hizbQuarter : hizbQuarters) {
                        for (int i = hizbQuarter.getAyaFrom(); i <= hizbQuarter.getAyaTo(); i++) {
                            ayaHezbQuarterIndex[i] = hizbQuarter.getId();
                        }
                    }
                    for (int i = 0; i < searchModels1.size(); i++) {
                        int hezbQuarterData = ayaHezbQuarterIndex[searchModels1.get(i).getId()];
                        int hezb = ((hezbQuarterData - 1) / 4) % 2 + 1;
                        int quarter = ((hezbQuarterData - 1) % 4) + 1;
                        searchModels1.get(i).setHezb(hezb);
                        searchModels1.get(i).setQuarter(quarter);
                    }
                    return searchModels1;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    listener.onGetTopics(result);
                }, error -> {
                    Log.d("Error", "Error");
                });
    }

    @Override
    public void searchAyaInGuz(String inputQuery, int guzNumber) {
        Single<List<SearchModel>> searchModels = mushafDatabase.getAyaDao()
                .getJuzSearchResult(inputQuery, guzNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        getData(searchModels);
    }

    @Override
    public void searchAyaInSura(String inputQuery, int suraNumber) {
        Single<List<SearchModel>> searchModels = mushafDatabase.getAyaDao()
                .getSuraSearchResult(inputQuery, suraNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        getData(searchModels);
    }

    @Override
    public LiveData<List<Integer>> getSurasInChapter(int chapter) {
        return mushafDatabase.getAyaDao().getSurasInChapter(chapter);
    }

    @Override
    public void searchWithSuraAndJuz(String inputSearch, int selectedSura, int selectedJuz) {
        Single<List<SearchModel>> searchModels = mushafDatabase.getAyaDao()
                .getSuraJuzSearchResult(inputSearch, selectedSura, selectedJuz)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        getData(searchModels);
    }

    @Override
    public void searchWithSuraAndJuzAndHizb(String inputSearch, int selectedSura, int selectedJuz, int selectedHezb) {
        int startHezbInterval = (selectedJuz - 1) * 8 + 1;
        if (selectedHezb == 2) {
            startHezbInterval += 4;
        }
        int endHezbInterval = startHezbInterval + 3;
        Single<List<SearchModel>> searchModels;
        if (selectedSura == 0) {
            searchModels = mushafDatabase.getAyaDao()
                    .getJuzHezbSearchResult(inputSearch, selectedJuz, startHezbInterval, endHezbInterval)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());

        } else {
            searchModels = mushafDatabase.getAyaDao()
                    .getSuraJuzHezbSearchResult(inputSearch, selectedSura, selectedJuz, startHezbInterval, endHezbInterval)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
        getData(searchModels);
    }

    @Override
    public void searchWithSuraAndJuzAndHizbQuarter(String inputSearch, int selectedSura, int selectedJuz, int selectedHezb, int selectedQuarter) {
        int startHezbInterval = (selectedJuz - 1) * 8 + 1 + (selectedQuarter - 1);
        if (selectedHezb == 2) {
            startHezbInterval += 4;
        }
        Single<List<SearchModel>> searchModels;
        if (selectedSura == 0) {
            searchModels = mushafDatabase.getAyaDao()
                    .getJuzHezbSearchResult(inputSearch, selectedJuz, startHezbInterval, startHezbInterval)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            searchModels = mushafDatabase.getAyaDao()
                    .getSuraJuzHezbSearchResult(inputSearch, selectedSura, selectedJuz, startHezbInterval, startHezbInterval)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
        getData(searchModels);
    }


}
