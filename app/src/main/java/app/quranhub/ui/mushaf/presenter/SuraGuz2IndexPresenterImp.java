package app.quranhub.ui.mushaf.presenter;

import android.content.Context;

import java.util.List;

import app.quranhub.ui.base.BasePresenterImp;
import app.quranhub.ui.base.BaseView;
import app.quranhub.ui.mushaf.interactor.SuraGuz2IndexInteractor;
import app.quranhub.ui.mushaf.interactor.SuraGuz2IndexInteractorImp;
import app.quranhub.ui.mushaf.model.SuraIndexModelMapper;
import app.quranhub.ui.mushaf.view.SuraGuz2IndexView;

public class SuraGuz2IndexPresenterImp<T extends BaseView> extends BasePresenterImp<T> implements SuraGuz2IndexPresenter<T>, SuraGuz2IndexInteractor.GetIndexListener {


    private final SuraGuz2IndexInteractor interactor;
    private final Context context;

    public SuraGuz2IndexPresenterImp(Context context) {
        this.interactor = new SuraGuz2IndexInteractorImp(this, context);
        this.context = context;
    }

    @Override
    public void getSuraIndex() {
        baseView.showLoading();
        interactor.getSuraIndex();
    }

    @Override
    public void onGetIndex(List<SuraIndexModelMapper> indexList) {
        if (isViewAttached() && baseView instanceof SuraGuz2IndexView) {
            baseView.hideLoading();
            ((SuraGuz2IndexView) baseView).onGetIndex(indexList);
        }
    }

    @Override
    public void onGetIndexFailed(String msg) {
        baseView.hideLoading();
        baseView.showMessage(msg);
    }
}
