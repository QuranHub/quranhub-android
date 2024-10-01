package app.quranhub.ui.mushaf.interactor;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import java.util.List;

import app.quranhub.data.local.db.MushafDatabase;
import app.quranhub.data.local.db.TranslationDatabase;
import app.quranhub.data.local.entity.Translation;
import app.quranhub.ui.mushaf.model.TafseerModel;

public class TafseerInteractorImp implements TafseerInteractor {

    private final Context context;
    @NonNull
    private final MushafDatabase mushafDatabase;
    private TranslationDatabase translationDatabase;


    public TafseerInteractorImp(@NonNull Context context) {
        this.context = context;
        mushafDatabase = MushafDatabase.getInstance(context.getApplicationContext());
    }

    @Override
    public void initTranslationDB(String dbName) {
        if (translationDatabase != null) {
            translationDatabase.close();
        }
        translationDatabase = TranslationDatabase.newInstance(context, dbName);
    }

    @Override
    public LiveData<List<Translation>> getSuraBookTafseers(int suraNumber) {
        return translationDatabase.getTranslationDao().getAyasTafseer(suraNumber);
    }

    @Override
    public LiveData<List<TafseerModel>> getSuraAyah(int suraNumber) {
        return mushafDatabase.getAyaDao().getPageTafseers(suraNumber);
    }


    @Override
    public LiveData<List<TafseerModel>> getSuraTafseers(int suraNumber) {

        return mushafDatabase.getAyaDao().getPageTafseers(suraNumber);
    }

}
