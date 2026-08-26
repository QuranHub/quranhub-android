package app.quranhub.ui.mushaf.interactor

import app.quranhub.ui.mushaf.model.SuraIndexModelMapper

interface SuraGuz2IndexInteractor {
    fun getSuraIndex()

    interface GetIndexListener {
        fun onGetIndex(indexList: List<SuraIndexModelMapper>)
        fun onGetIndexFailed(msg: String)
    }
}
