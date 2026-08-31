package app.quranhub.ui.mushaf.interactor

import app.quranhub.ui.mushaf.model.HizbQuarterDataModel
import kotlinx.coroutines.flow.Flow

interface Guz2IndexInteractor {
    val allHizbQuarterDataModel: Flow<List<HizbQuarterDataModel>>
}
