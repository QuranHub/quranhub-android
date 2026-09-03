package app.quranhub.ui.mushaf.interactor

import android.content.Context
import android.os.Environment
import android.util.Log
import app.quranhub.data.Constants
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.TranslationDatabase
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.AyaRecorder
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.ui.mushaf.model.PageSuras
import app.quranhub.ui.mushaf.model.QuranPageInfo
import app.quranhub.ui.mushaf.model.SuraVersesNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class Mus7fInteractorImp(context: Context) : Mus7fInteractor {

    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context)
    private val userDatabase: UserDatabase = UserDatabase.getInstance(context)

    @Volatile
    private var translationDatabase: TranslationDatabase? = null

    private val context: Context = context

    override fun initTranslationDB(dbName: String) {
        translationDatabase = TranslationDatabase.newInstance(context, dbName)
    }

    override suspend fun getPageInfo(currentPage: Int): QuranPageInfo? =
        withContext(Dispatchers.IO) {
            mushafDatabase.suraDao.getQuranPageInfo(currentPage)
        }

    override suspend fun getAyaTafseer(ayaId: Int): String? = withContext(Dispatchers.IO) {
        translationDatabase?.translationDao?.findByIndex(ayaId)
    }

    override suspend fun getTafseerBook(currentTafsserId: String): TranslationBook? =
        withContext(Dispatchers.IO) {
            userDatabase.translationBookDao.findById(currentTafsserId)
        }

    override suspend fun getPageSuras(): ArrayList<ArrayList<Int>> =
        withContext(Dispatchers.IO) {
            val pageSuras: List<PageSuras> = mushafDatabase.ayaDao.getSuraPage()
            var surasInPage = ArrayList<Int>()
            val results = ArrayList<ArrayList<Int>>()
            for (i in pageSuras.indices) {
                if (i == 0) {
                    surasInPage.add(pageSuras[i].sura)
                    results.add(surasInPage)
                    surasInPage = ArrayList()
                }
                if (i == pageSuras.size - 1) {
                    surasInPage.add(pageSuras[i].sura)
                    results.add(surasInPage)
                } else if (pageSuras[i].page != pageSuras[i + 1].page) {
                    surasInPage.add(pageSuras[i].sura)
                    results.add(surasInPage)
                    surasInPage = ArrayList()
                } else {
                    surasInPage.add(pageSuras[i].sura)
                }
            }
            results.add(surasInPage)
            results
        }

    override suspend fun getSuraNumofVerses(): ArrayList<SuraVersesNumber> =
        withContext(Dispatchers.IO) {
            ArrayList(mushafDatabase.suraDao.getSuraVersesNumber())
        }

    override suspend fun getFromAyaPage(fromAya: Int): Int? = withContext(Dispatchers.IO) {
        mushafDatabase.ayaDao.getAyaPage(fromAya)
    }

    override suspend fun checkAyaHasRecorder(id: Int): String? = withContext(Dispatchers.IO) {
        val recitation = AppPreferencesManager.getRecitationSetting(context)
        userDatabase.quranAudioDao.getAyaRecorderPath(id, recitation)
    }

    override suspend fun saveRecorderPath(ayaId: Int, recorderPath: String) {
        withContext(Dispatchers.IO) {
            val recitation = AppPreferencesManager.getRecitationSetting(context)
            val ayaRecorder = AyaRecorder(ayaId, recitation, recorderPath)
            userDatabase.quranAudioDao.insertAyaRecorder(ayaRecorder)
        }
    }

    override suspend fun deleteAyaVoiceRecorder(ayaId: Int) {
        withContext(Dispatchers.IO) {
            val recitation = AppPreferencesManager.getRecitationSetting(context)
            userDatabase.quranAudioDao.deleteAyaVoiceRecorder(ayaId, recitation)
            deleteRecorderLocally(ayaId, recitation)
        }
    }

    override suspend fun getAya(currentAyaId: Int): Aya? = withContext(Dispatchers.IO) {
        mushafDatabase.ayaDao.findAyaById(currentAyaId)
    }

    private fun deleteRecorderLocally(ayaId: Int, recitation: Int) {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            Constants.Directory.AYA_VOICE_RECORDER + File.separator + recitation + File.separator + ayaId + ".3gp"
        )
        if (file.exists()) {
            file.delete()
        }
    }

    companion object {
        private const val TAG = "Mus7fInteractorImp"
    }
}
