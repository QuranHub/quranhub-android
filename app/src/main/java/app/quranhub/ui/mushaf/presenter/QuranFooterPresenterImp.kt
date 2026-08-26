package app.quranhub.ui.mushaf.presenter

import app.quranhub.ui.base.BasePresenterImp
import app.quranhub.ui.mushaf.view.QuranFooterView

class QuranFooterPresenterImp : BasePresenterImp<QuranFooterView>(), QuranFooterPresenter {

    override fun displaySearchDialog() {
        baseView!!.displaySearchDialog()
    }

    override fun toggleNightMode() {
        baseView!!.toggleNightMode()
    }
}
