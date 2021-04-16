package app.quranhub.ui.mushaf.presenter;

import app.quranhub.ui.base.BasePresenter;
import app.quranhub.ui.base.BaseView;
import app.quranhub.data.local.entity.TranslationBook;

public interface Mus7fPresenter<T extends BaseView> extends BasePresenter<T> {

    void getQuranPageInfo(int curentPage);

    void setNightMode(boolean nightMode);

    boolean getNightMode();

    boolean toggleNightMode();

    void getAyaTafseer(int ayaId);

    void getCurrentTafseerBook(String currentTafsserId);

    void onGetTafsserBook(TranslationBook book);

    void getSurasInPage();

    void checkAyaHasRecorder(int id);

    void saveRecorderPath(int ayaId, String recorderPath);

    void deleteAyaVoiceRecorder(int ayaId);

    void getSuraNumofVerses();

    void getFromAyaPage(int fromAya);

    void getNotificationAya(int ayaId);

}
