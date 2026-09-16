package app.quranhub.feature.mushaf.interactor

import android.content.Context
import app.quranhub.core.data.local.db.MushafDatabase
import app.quranhub.core.data.model.HizbQuarterDataModel
import kotlinx.coroutines.flow.Flow

class Guz2IndexInteractorImp(context: Context) : Guz2IndexInteractor {

    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context)

    override val allHizbQuarterDataModel: Flow<List<HizbQuarterDataModel>>
        get() = mushafDatabase.hizbQuarterDao.getAllHizbQuarterDataModel()
}
