package app.quranhub.feature.mushaf.interactor

import app.quranhub.core.data.model.HizbQuarterDataModel
import kotlinx.coroutines.flow.Flow

interface Guz2IndexInteractor {
    val allHizbQuarterDataModel: Flow<List<HizbQuarterDataModel>>
}
