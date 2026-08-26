package app.quranhub.ui.mushaf.presenter

import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.ui.base.BasePresenter
import app.quranhub.ui.base.BaseView

interface Mus7fPresenter<T : BaseView> : BasePresenter<T> {
    fun getQuranPageInfo(curentPage: Int)
    fun setNightMode(nightMode: Boolean)
    val nightMode: Boolean
    val quranPageZoomScaleFactor: Float
    fun toggleNightMode(): Boolean
    fun getAyaTafseer(ayaId: Int)
    fun getCurrentTafseerBook(currentTafsserId: String)
    fun onGetTafsserBook(book: TranslationBook)
    fun getSurasInPage()
    fun checkAyaHasRecorder(id: Int)
    fun saveRecorderPath(ayaId: Int, recorderPath: String)
    fun deleteAyaVoiceRecorder(ayaId: Int)
    fun getSuraNumofVerses()
    fun getFromAyaPage(fromAya: Int)
    fun getNotificationAya(ayaId: Int)
}
