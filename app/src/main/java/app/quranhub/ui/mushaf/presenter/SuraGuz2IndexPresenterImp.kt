package app.quranhub.ui.mushaf.presenter

import android.content.Context
import app.quranhub.ui.base.BasePresenterImp
import app.quranhub.ui.base.BaseView
import app.quranhub.ui.mushaf.interactor.SuraGuz2IndexInteractor
import app.quranhub.ui.mushaf.interactor.SuraGuz2IndexInteractorImp
import app.quranhub.ui.mushaf.model.SuraIndexModelMapper
import app.quranhub.ui.mushaf.view.SuraGuz2IndexView

class SuraGuz2IndexPresenterImp<T : BaseView>(context: Context) : BasePresenterImp<T>(),
    SuraGuz2IndexPresenter<T>, SuraGuz2IndexInteractor.GetIndexListener {

    private val interactor: SuraGuz2IndexInteractor = SuraGuz2IndexInteractorImp(this, context)
    private val context: Context = context

    override fun getSuraIndex() {
        baseView!!.showLoading()
        interactor.getSuraIndex()
    }

    override fun onGetIndex(indexList: List<SuraIndexModelMapper>) {
        if (isViewAttached && baseView is SuraGuz2IndexView) {
            baseView!!.hideLoading()
            (baseView as SuraGuz2IndexView).onGetIndex(indexList)
        }
    }

    override fun onGetIndexFailed(msg: String) {
        baseView!!.hideLoading()
        baseView!!.showMessage(msg)
    }
}
