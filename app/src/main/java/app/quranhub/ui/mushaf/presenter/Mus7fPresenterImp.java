package app.quranhub.ui.mushaf.presenter;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import app.quranhub.R;
import app.quranhub.data.Constants;
import app.quranhub.data.local.entity.Aya;
import app.quranhub.data.local.entity.TranslationBook;
import app.quranhub.data.local.prefs.AppPreferencesManager;
import app.quranhub.ui.base.BasePresenterImp;
import app.quranhub.ui.mushaf.interactor.Mus7fInteractor;
import app.quranhub.ui.mushaf.interactor.Mus7fInteractorImp;
import app.quranhub.ui.mushaf.model.QuranPageInfo;
import app.quranhub.ui.mushaf.model.SuraVersesNumber;
import app.quranhub.ui.mushaf.view.MushafView;

public class Mus7fPresenterImp extends BasePresenterImp<MushafView>
        implements Mus7fPresenter<MushafView>, Mus7fInteractor.ResultListener {

    private static final String TAG = Mus7fPresenterImp.class.getSimpleName();

    @NonNull
    private final Mus7fInteractor interactor;
    @NonNull
    private final Context context;

    public Mus7fPresenterImp(@NonNull Context context) {
        this.context = context;
        this.interactor = new Mus7fInteractorImp(this, context);
    }

    @Override
    public void getQuranPageInfo(int currentPage) {
        currentPage = Constants.Quran.NUM_OF_PAGES - currentPage;
        interactor.getPageInfo(currentPage);
    }

    @Override
    public void setNightMode(boolean nightMode) {
        AppPreferencesManager.persistNightModeSetting(context, nightMode);
    }

    @Override
    public boolean getNightMode() {
        return AppPreferencesManager.getNightModeSetting(context);
    }

    @Override
    public boolean toggleNightMode() {
        boolean nightMode = !getNightMode();
        setNightMode(nightMode);
        return nightMode;
    }

    @Override
    public float getQuranPageZoomScaleFactor() {
        return AppPreferencesManager.getQuranPageZoomScaleSetting(context);
    }

    @Override
    public void getAyaTafseer(int ayaId) {
        interactor.getAyaTafseer(ayaId);
    }

    @Override
    public void getCurrentTafseerBook(String currentTafsserId) {
        interactor.getTafseerBook(currentTafsserId);
    }

    @Override
    public void onGetPageInfo(QuranPageInfo pageInfo) {
        if (isViewAttached()) {
            baseView.showQuranPageInfo(pageInfo);
        }
    }

    @Override
    public void onGetAyaTafseer(String tafseer) {
        if (isViewAttached()) {
            baseView.onGetAyaTafseer(tafseer);
        }
    }

    @Override
    public void onGetTafsserBook(TranslationBook book) {
        if (isViewAttached()) {
            interactor.initTranslationDB(book.getDatabaseName());
            baseView.onGetTafseerBook(book);
        }
    }

    @Override
    public void getSurasInPage() {
        interactor.getPageSuras();
    }


    @Override
    public void onGetSuraPage(ArrayList<ArrayList<Integer>> suras) {
        if (isViewAttached()) {
            baseView.onGetPageSuras(suras);
        }
    }

    @Override
    public void onErrorOccurred() {
        if (isViewAttached()) {
            baseView.showMessage(context.getString(R.string.page_info_failed));
        }
    }

    @Override
    public void checkAyaHasRecorder(int id) {
        interactor.checkAyaHasRecorder(id);
    }

    @Override
    public void saveRecorderPath(int ayaId, String recorderPath) {
        if (isViewAttached()) {
            interactor.saveRecorderPath(ayaId, recorderPath);
        }
    }

    @Override
    public void deleteAyaVoiceRecorder(int ayaId) {
        if (isViewAttached()) {
            interactor.deleteAyaVoiceRecorder(ayaId);
        }
    }

    @Override
    public void getSuraNumofVerses() {
        if (isViewAttached()) {
            interactor.getSuraNumofVerses();
        }
    }

    @Override
    public void getFromAyaPage(int fromAya) {
        if (isViewAttached()) {
            interactor.getFromAyaPage(fromAya);
        }
    }

    @Override
    public void getNotificationAya(int ayaId) {
        if (isViewAttached()) {
            interactor.getAya(ayaId);
        }
    }

    @Override
    public void onNoBooks() {
        baseView.onNoBooksExist();
    }

    @Override
    public void onAyaHasRecorder(String recorderPath) {
        if (isViewAttached()) {
            baseView.onGetAyaRecorder(recorderPath);
        }
    }

    @Override
    public void onGetSuraVersesNumber(ArrayList<SuraVersesNumber> suraVersesNumbers) {
        if (isViewAttached()) {
            baseView.onGetSuraVersesNumber(suraVersesNumbers);
        }
    }

    @Override
    public void onGetAyaPage(int page) {
        if (isViewAttached()) {
            baseView.onGetAyaPage(page);
        }
    }

    @Override
    public void onGetAya(Aya aya) {
        if (isViewAttached()) {
            baseView.onGetCurrentAyaFromNotification(aya);
        }
    }


}
