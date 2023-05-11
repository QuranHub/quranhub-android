package app.quranhub.ui.mushaf.listener;

public interface QuranNavigationCallbacks {

    void gotoQuranPage(int pageNumber);

    void gotoQuranPageAya(int pageNumber, int ayaId, boolean addToStack);

}
