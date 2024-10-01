package app.quranhub.ui.mushaf.interactor;

import java.util.List;

import app.quranhub.ui.mushaf.model.SuraIndexModelMapper;

public interface SuraGuz2IndexInteractor {

    void getSuraIndex();

    interface GetIndexListener {
        void onGetIndex(List<SuraIndexModelMapper> indexList);

        void onGetIndexFailed(String msg);
    }
}
