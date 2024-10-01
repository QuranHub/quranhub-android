package app.quranhub.ui.mushaf.interactor;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;

import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel;

public class Guz2IndexInteractorImp implements Guz2IndexInteractor {

    @NonNull
    private final Context context;
    @NonNull
    private final MushafDatabase mushafDatabase;


    public Guz2IndexInteractorImp(@NonNull Context appConext) {
        context = appConext;
        mushafDatabase = MushafDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public LiveData<List<HizbQuarterDataModel>> getAllHizbQuarterDataModel() {
        return mushafDatabase.getHizbQuarterDao().getAllHizbQuarterDataModel();
    }
}
