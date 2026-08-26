package app.quranhub.ui.mushaf.presenter

import android.content.Context
import app.quranhub.R
import app.quranhub.data.Constants
import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.ui.base.BasePresenterImp
import app.quranhub.ui.mushaf.interactor.Mus7fInteractor
import app.quranhub.ui.mushaf.interactor.Mus7fInteractorImp
import app.quranhub.ui.mushaf.model.QuranPageInfo
import app.quranhub.ui.mushaf.model.SuraVersesNumber
import app.quranhub.ui.mushaf.view.MushafView

class Mus7fPresenterImp(context: Context) : BasePresenterImp<MushafView>(),
    Mus7fPresenter<MushafView>, Mus7fInteractor.ResultListener {

    private val interactor: Mus7fInteractor = Mus7fInteractorImp(this, context)
    private val context: Context = context

    override fun getQuranPageInfo(currentPage: Int) {
        val adjustedPage = Constants.Quran.NUM_OF_PAGES - currentPage
        interactor.getPageInfo(adjustedPage)
    }

    override fun setNightMode(nightMode: Boolean) {
        AppPreferencesManager.persistNightModeSetting(context, nightMode)
    }

    override val nightMode: Boolean
        get() = AppPreferencesManager.getNightModeSetting(context)

    override fun toggleNightMode(): Boolean {
        val newNightMode = !nightMode
        setNightMode(newNightMode)
        return newNightMode
    }

    override val quranPageZoomScaleFactor: Float
        get() = AppPreferencesManager.getQuranPageZoomScaleSetting(context)

    override fun getAyaTafseer(ayaId: Int) {
        interactor.getAyaTafseer(ayaId)
    }

    override fun getCurrentTafseerBook(currentTafsserId: String) {
        interactor.getTafseerBook(currentTafsserId)
    }

    override fun onGetPageInfo(pageInfo: QuranPageInfo) {
        if (isViewAttached) {
            baseView!!.showQuranPageInfo(pageInfo)
        }
    }

    override fun onGetAyaTafseer(tafseer: String) {
        if (isViewAttached) {
            baseView!!.onGetAyaTafseer(tafseer)
        }
    }

    override fun onGetTafsserBook(book: TranslationBook) {
        if (isViewAttached) {
            interactor.initTranslationDB(book.databaseName)
            baseView!!.onGetTafseerBook(book)
        }
    }

    override fun getSurasInPage() {
        interactor.getPageSuras()
    }

    override fun onGetSuraPage(suras: ArrayList<ArrayList<Int>>) {
        if (isViewAttached) {
            baseView!!.onGetPageSuras(suras)
        }
    }

    override fun onErrorOccurred() {
        if (isViewAttached) {
            baseView!!.showMessage(context.getString(R.string.page_info_failed))
        }
    }

    override fun checkAyaHasRecorder(id: Int) {
        interactor.checkAyaHasRecorder(id)
    }

    override fun saveRecorderPath(ayaId: Int, recorderPath: String) {
        if (isViewAttached) {
            interactor.saveRecorderPath(ayaId, recorderPath)
        }
    }

    override fun deleteAyaVoiceRecorder(ayaId: Int) {
        if (isViewAttached) {
            interactor.deleteAyaVoiceRecorder(ayaId)
        }
    }

    override fun getSuraNumofVerses() {
        if (isViewAttached) {
            interactor.getSuraNumofVerses()
        }
    }

    override fun getFromAyaPage(fromAya: Int) {
        if (isViewAttached) {
            interactor.getFromAyaPage(fromAya)
        }
    }

    override fun getNotificationAya(ayaId: Int) {
        if (isViewAttached) {
            interactor.getAya(ayaId)
        }
    }

    override fun onNoBooks() {
        baseView!!.onNoBooksExist()
    }

    override fun onAyaHasRecorder(recorderPath: String) {
        if (isViewAttached) {
            baseView!!.onGetAyaRecorder(recorderPath)
        }
    }

    override fun onGetSuraVersesNumber(suraVersesNumbers: ArrayList<SuraVersesNumber>) {
        if (isViewAttached) {
            baseView!!.onGetSuraVersesNumber(suraVersesNumbers)
        }
    }

    override fun onGetAyaPage(page: Int) {
        if (isViewAttached) {
            baseView!!.onGetAyaPage(page)
        }
    }

    override fun onGetAya(aya: Aya) {
        if (isViewAttached) {
            baseView!!.onGetCurrentAyaFromNotification(aya)
        }
    }
}
