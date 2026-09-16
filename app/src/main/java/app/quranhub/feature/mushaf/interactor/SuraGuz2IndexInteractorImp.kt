package app.quranhub.feature.mushaf.interactor

import android.content.Context
import app.quranhub.core.data.local.db.MushafDatabase
import app.quranhub.core.data.model.SuraIndexModel

class SuraGuz2IndexInteractorImp(context: Context) : SuraGuz2IndexInteractor {

    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context)

    override suspend fun getSuraIndex(): List<SuraIndexModel> {
        return mushafDatabase.suraDao.getSuraIndexInfo()
    }
}
