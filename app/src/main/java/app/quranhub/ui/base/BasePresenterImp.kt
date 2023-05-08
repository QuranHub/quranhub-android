package app.quranhub.ui.base

open class BasePresenterImp<T : BaseView?> : BasePresenter<T> {

    @JvmField
    protected var baseView: T? = null

    override val isViewAttached: Boolean
        get() = baseView != null

    override fun onAttach(view: T) {
        baseView = view
    }

    override fun onDetach() {
        baseView = null
    }
}