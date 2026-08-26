package app.quranhub.ui.mushaf.view

import app.quranhub.ui.base.BaseView
import app.quranhub.ui.mushaf.model.SuraIndexModelMapper

interface SuraGuz2IndexView : BaseView {
    fun onGetIndex(indexList: List<SuraIndexModelMapper>)
}
