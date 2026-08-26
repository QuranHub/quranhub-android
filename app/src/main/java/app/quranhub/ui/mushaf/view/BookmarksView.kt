package app.quranhub.ui.mushaf.view

import app.quranhub.ui.base.BaseView

interface BookmarksView : BaseView {
    fun enableEditList()
    fun finishEditList()
    fun filterList()
    fun searchList(text: String)
}
