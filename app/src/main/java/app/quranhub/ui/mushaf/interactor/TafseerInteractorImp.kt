package app.quranhub.ui.mushaf.interactor

import android.content.Context
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.TranslationDatabase
import app.quranhub.data.local.entity.Translation
import app.quranhub.ui.mushaf.model.TafseerModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TafseerInteractorImp(context: Context) : TafseerInteractor {

    private val context: Context = context
    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context.applicationContext)
    private var translationDatabase: TranslationDatabase? = null

    override suspend fun initTranslationDB(dbName: String?) {
        withContext(Dispatchers.IO) {
            translationDatabase?.close()
            translationDatabase = dbName?.let { TranslationDatabase.newInstance(context, it) }
        }
    }

    override fun getSuraBookTafseers(suraNumber: Int): Flow<List<Translation>> {
        val database = checkNotNull(translationDatabase) {
            "Translation database not initialised; call initTranslationDB first"
        }
        return database.translationDao.getAyasTafseer(suraNumber)
    }

    override fun getSuraTafseers(suraNumber: Int): Flow<List<TafseerModel>> {
        return mushafDatabase.ayaDao.getPageTafseers(suraNumber)
    }
}
