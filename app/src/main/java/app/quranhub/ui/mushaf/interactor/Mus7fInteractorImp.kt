package app.quranhub.ui.mushaf.interactor

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import app.quranhub.data.Constants
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.TranslationDatabase
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.AyaRecorder
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.ui.mushaf.model.SuraVersesNumber
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File

class Mus7fInteractorImp(
    private val resultListener: Mus7fInteractor.ResultListener,
    context: Context
) : Mus7fInteractor {

    private val mushafDatabase: MushafDatabase = MushafDatabase.getInstance(context)
    private val userDatabase: UserDatabase = UserDatabase.getInstance(context)
    private var translationDatabase: TranslationDatabase? = null
    private val context: Context = context
    private val chosenRecitation = -1
    private var chosenSheikh: String? = null

    override fun initTranslationDB(dbName: String) {
        translationDatabase = TranslationDatabase.newInstance(context, dbName)
    }

    @SuppressLint("CheckResult")
    override fun getPageSuras() {
        mushafDatabase.ayaDao.getSuraPage()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { pageSuras: List<app.quranhub.ui.mushaf.model.PageSuras> ->
                var surasInPage = mutableListOf<Int>()
                val results = mutableListOf<MutableList<Int>>()
                for (i in pageSuras.indices) {
                    if (i == 0) {
                        surasInPage.add(pageSuras[i].sura)
                        results.add(surasInPage)
                        surasInPage = mutableListOf()
                    }
                    if (i == pageSuras.size - 1) {
                        surasInPage.add(pageSuras[i].sura)
                        results.add(surasInPage)
                    } else if (pageSuras[i].page != pageSuras[i + 1].page) {
                        surasInPage.add(pageSuras[i].sura)
                        results.add(surasInPage)
                        surasInPage = mutableListOf()
                    } else {
                        surasInPage.add(pageSuras[i].sura)
                    }
                }
                results.add(surasInPage)
                results
            }
            .subscribe({ result ->
                resultListener.onGetSuraPage(result as ArrayList<ArrayList<Int>>)
            }, {
            })
    }

    @SuppressLint("CheckResult")
    override fun getSuraNumofVerses() {
        mushafDatabase.suraDao.getSuraVersesNumber()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res ->
                resultListener.onGetSuraVersesNumber(res as ArrayList<SuraVersesNumber>)
            }, {
                Log.d(TAG, "Failed getSuraNumofVerses: ")
            })
    }

    @SuppressLint("CheckResult")
    override fun getFromAyaPage(fromAya: Int) {
        mushafDatabase.ayaDao.getAyaPage(fromAya)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ res -> resultListener.onGetAyaPage(res) }, {
                Log.d(TAG, "Failed getFromAyaPage: ")
            })
    }

    @SuppressLint("CheckResult")
    override fun getPageInfo(curentPage: Int) {
        mushafDatabase.suraDao.getQuranPageInfo(curentPage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                if (result != null) {
                    resultListener.onGetPageInfo(result)
                } else {
                    resultListener.onErrorOccurred()
                }
            }, { error ->
                Log.d(TAG, "getPageInfo: $error")
                resultListener.onErrorOccurred()
            })
    }

    @SuppressLint("CheckResult")
    override fun getTafseerBook(currentTafsserId: String) {
        userDatabase.translationBookDao.findById(currentTafsserId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ res ->
                resultListener.onGetTafsserBook(res)
            }, {
                resultListener.onNoBooks()
            })
    }

    @SuppressLint("CheckResult")
    override fun getAyaTafseer(ayaId: Int) {
        if (translationDatabase != null) {
            translationDatabase!!.translationDao
                .findByIndex(ayaId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ res ->
                    resultListener.onGetAyaTafseer(res)
                }, {
                    resultListener.onErrorOccurred()
                })
        }
    }

    @SuppressLint("CheckResult")
    override fun checkAyaHasRecorder(id: Int) {
        val recitation = AppPreferencesManager.getRecitationSetting(context)
        userDatabase.quranAudioDao.getAyaRecorderPath(id, recitation)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ path ->
                resultListener.onAyaHasRecorder(path)
            }, {
                Log.e(TAG, "checkAyaHasRecorder: No recorder exist")
            })
    }

    override fun saveRecorderPath(ayaId: Int, recorderPath: String) {
        val recitation = AppPreferencesManager.getRecitationSetting(context)
        val ayaRecorder = AyaRecorder(ayaId, recitation, recorderPath)
        Completable.fromAction {
            userDatabase.quranAudioDao.insertAyaRecorder(ayaRecorder)
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({}, {})
    }

    @SuppressLint("CheckResult")
    override fun getAya(currentAyaId: Int) {
        mushafDatabase.ayaDao.findById(currentAyaId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                resultListener.onGetAya(result)
            }, {
                Log.e(TAG, "onError: getAya")
            })
    }

    override fun deleteAyaVoiceRecorder(ayaId: Int) {
        val recitation = AppPreferencesManager.getRecitationSetting(context)
        Completable.fromAction {
            userDatabase.quranAudioDao.deleteAyaVoiceRecorder(ayaId, recitation)
        }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({}, {})
            .also {
                deleteRecorderLocally(ayaId, recitation)
            }
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
