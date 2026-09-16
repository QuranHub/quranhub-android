package app.quranhub.feature.mushaf.interactor

import app.quranhub.core.data.model.SuraIndexModel

interface SuraGuz2IndexInteractor {
    suspend fun getSuraIndex(): List<SuraIndexModel>
}
