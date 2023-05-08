package app.quranhub.ui.base

interface BasePresenter<T : BaseView?> {

    val isViewAttached: Boolean

    fun onAttach(view: T)

    fun onDetach()
}