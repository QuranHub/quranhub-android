package app.quranhub.ui.mushaf.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import app.quranhub.ui.mushaf.interactor.Guz2IndexInteractor;
import app.quranhub.ui.mushaf.interactor.Guz2IndexInteractorImp;
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel;

public class Guz2IndexViewModel extends AndroidViewModel {

    @NonNull
    private Context context;
    @NonNull
    private Guz2IndexInteractor guz2IndexInteractor;

    private LiveData<List<HizbQuarterDataModel>> hizbQuarterDataModelsLiveData;
    @NonNull
    private MutableLiveData<IndexItemClickEvent> indexItemClickEvent = new MutableLiveData<>();


    public Guz2IndexViewModel(@NonNull Application application) {
        super(application);
        context = application;
        guz2IndexInteractor = new Guz2IndexInteractorImp(application);
    }

    @NonNull
    public LiveData<List<HizbQuarterDataModel>> getHizbQuarterDataModelsLiveData() {
        if (hizbQuarterDataModelsLiveData == null) {
            hizbQuarterDataModelsLiveData = guz2IndexInteractor.getAllHizbQuarterDataModel();
        }
        return hizbQuarterDataModelsLiveData;
    }

    @NonNull
    public LiveData<IndexItemClickEvent> indexItemClickEvent() {
        return indexItemClickEvent;
    }

    public void notifyIndexItemClick(int clickedItemIndex) {
        HizbQuarterDataModel model = hizbQuarterDataModelsLiveData.getValue().get(clickedItemIndex);
        indexItemClickEvent.setValue(new IndexItemClickEvent(model.getStartPage()));
    }


    public static class IndexItemClickEvent {

        public int page;

        public IndexItemClickEvent(int page) {
            this.page = page;
        }
    }
}
