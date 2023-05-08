package app.quranhub.ui.base;

public class BasePresenterImp<T extends BaseView> implements BasePresenter<T> {

    protected T baseView = null;

    @Override
    public void onAttach(T view) {
        this.baseView = view;
    }

    @Override
    public void onDetach() {
        baseView = null;
    }

    @Override
    public boolean isViewAttached() {
        return baseView != null;
    }


}
