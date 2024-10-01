package app.quranhub.ui.mushaf.interactor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.entity.HizbQuarter;
import app.quranhub.ui.mushaf.model.SearchModel;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TopicInteractorImp implements TopicInteractor {

    private final Context context;
    @NonNull
    private final MushafDatabase mushafDatabase;
    private final TopicInteractor.TopicListener listener;

    public TopicInteractorImp(@NonNull Context context, TopicInteractor.TopicListener listener) {
        this.context = context;
        mushafDatabase = MushafDatabase.getInstance(context.getApplicationContext());
        this.listener = listener;
    }

    @SuppressLint("CheckResult")
    @Override
    public void getAyas(int categoryId) {

        Single<List<SearchModel>> topicAyas = mushafDatabase.getAyaDao().getCategoryAyas(categoryId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Single<List<HizbQuarter>> hizbQuarterData = mushafDatabase.getHizbQuarterDao().getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Single.zip(topicAyas, hizbQuarterData, (searchModels, hizbQuarters) -> {

                    int[] ayaHezbQuarterIndex = new int[6237];
                    for (HizbQuarter hizbQuarter : hizbQuarters) {
                        for (int i = hizbQuarter.getAyaFrom(); i <= hizbQuarter.getAyaTo(); i++) {
                            ayaHezbQuarterIndex[i] = hizbQuarter.getId();
                        }
                    }
                    for (int i = 0; i < searchModels.size(); i++) {
                        int hezbQuarterData = ayaHezbQuarterIndex[searchModels.get(i).getId()];
                        int hezb = ((hezbQuarterData - 1) / 4) % 2 + 1;
                        int quarter = ((hezbQuarterData - 1) % 4) + 1;
                        searchModels.get(i).setHezb(hezb);
                        searchModels.get(i).setQuarter(quarter);
                    }
                    return searchModels;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    listener.onGetTopics(result);
                }, error -> {
                    Log.d("Error", "Error");
                });

    }
}
