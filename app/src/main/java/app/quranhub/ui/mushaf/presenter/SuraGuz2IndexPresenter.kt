package app.quranhub.ui.mushaf.presenter

import app.quranhub.ui.base.BasePresenter
import app.quranhub.ui.base.BaseView

interface SuraGuz2IndexPresenter<T : BaseView> : BasePresenter<T> {
    fun getSuraIndex()
}
