package app.quranhub.ui.mushaf.interactor

import android.content.Context
import androidx.lifecycle.LiveData
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.TranslationDatabase
import app.quranhub.data.local.entity.Translation
import app.quranhub.ui.mushaf.model.TafseerModel

class TafseerInteractorImp(context: Context) : TafseerInteractor {

    private val context: Context = context
    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context.applicationContext)
    private var translationDatabase: TranslationDatabase? = null

    override fun initTranslationDB(dbName: String?) {
        if (translationDatabase != null) {
            translationDatabase!!.close()
        }
        if (dbName != null) {
            translationDatabase = TranslationDatabase.newInstance(context, dbName)
        }
    }

    override fun getSuraBookTafseers(suraNumber: Int): LiveData<List<Translation>> {
        @Suppress("UNCHECKED_CAST")
        return translationDatabase!!.translationDao.getAyasTafseer(suraNumber) as LiveData<List<Translation>>
    }

    override fun getSuraAyah(suraNumber: Int): LiveData<List<TafseerModel>> {
        @Suppress("UNCHECKED_CAST")
        return mushafDatabase.ayaDao.getPageTafseers(suraNumber) as LiveData<List<TafseerModel>>
    }

    override fun getSuraTafseers(suraNumber: Int): LiveData<List<TafseerModel>> {
        @Suppress("UNCHECKED_CAST")
        return mushafDatabase.ayaDao.getPageTafseers(suraNumber) as LiveData<List<TafseerModel>>
    }
}
