package app.quranhub.ui.mushaf.presenter

import app.quranhub.ui.base.BasePresenter
import app.quranhub.ui.base.BaseView

interface BookmarksPresenter<T : BaseView> : BasePresenter<T> {
    fun enableEditList()
    fun finishEditList()
    fun filterList()
    fun searchList(text: String)
}
