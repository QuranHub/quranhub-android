package app.quranhub.ui.mushaf.presenter

import app.quranhub.ui.base.BasePresenter
import app.quranhub.ui.mushaf.view.QuranFooterView

interface QuranFooterPresenter : BasePresenter<QuranFooterView> {
    fun displaySearchDialog()
    fun toggleNightMode()
}
