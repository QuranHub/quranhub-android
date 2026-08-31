package app.quranhub.ui.mushaf.interactor

import app.quranhub.ui.mushaf.model.SuraIndexModel

interface SuraGuz2IndexInteractor {
    suspend fun getSuraIndex(): List<SuraIndexModel>
}
