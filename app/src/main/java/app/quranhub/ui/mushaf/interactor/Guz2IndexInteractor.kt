package app.quranhub.ui.mushaf.interactor

import androidx.lifecycle.LiveData
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel

interface Guz2IndexInteractor {
    val allHizbQuarterDataModel: LiveData<List<HizbQuarterDataModel>>
}
