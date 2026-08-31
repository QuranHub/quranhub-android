package app.quranhub.ui.mushaf.interactor

import android.content.Context
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.ui.mushaf.model.SuraIndexModel

class SuraGuz2IndexInteractorImp(context: Context) : SuraGuz2IndexInteractor {

    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context)

    override suspend fun getSuraIndex(): List<SuraIndexModel> {
        return mushafDatabase.suraDao.getSuraIndexInfo()
    }
}
