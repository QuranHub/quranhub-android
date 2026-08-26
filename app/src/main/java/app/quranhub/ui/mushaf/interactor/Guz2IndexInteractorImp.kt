package app.quranhub.ui.mushaf.interactor

import android.content.Context
import androidx.lifecycle.LiveData
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel

class Guz2IndexInteractorImp(context: Context) : Guz2IndexInteractor {

    private val context: Context = context
    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context)

    override val allHizbQuarterDataModel: LiveData<List<HizbQuarterDataModel>>
        get() {
            @Suppress("UNCHECKED_CAST")
            return mushafDatabase.hizbQuarterDao.getAllHizbQuarterDataModel() as LiveData<List<HizbQuarterDataModel>>
        }
}
