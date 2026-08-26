package app.quranhub.ui.mushaf.interactor

import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.ui.mushaf.model.QuranPageInfo
import app.quranhub.ui.mushaf.model.SuraVersesNumber

interface Mus7fInteractor {
    fun getPageInfo(curentPage: Int)
    fun getAyaTafseer(ayaId: Int)
    fun getTafseerBook(currentTafsserId: String)
    fun initTranslationDB(dbName: String)
    fun getPageSuras()
    fun checkAyaHasRecorder(id: Int)
    fun saveRecorderPath(ayaId: Int, recorderPath: String)
    fun deleteAyaVoiceRecorder(ayaId: Int)
    fun getSuraNumofVerses()
    fun getFromAyaPage(fromAya: Int)
    fun getAya(currentAyaId: Int)

    interface ResultListener {
        fun onGetPageInfo(pageInfo: QuranPageInfo)
        fun onGetAyaTafseer(tafseer: String)
        fun onGetTafsserBook(book: TranslationBook)
        fun onGetSuraPage(suras: ArrayList<ArrayList<Int>>)
        fun onErrorOccurred()
        fun onNoBooks()
        fun onAyaHasRecorder(recorderPath: String)
        fun onGetSuraVersesNumber(suraVersesNumbers: ArrayList<SuraVersesNumber>)
        fun onGetAyaPage(page: Int)
        fun onGetAya(aya: Aya)
    }
}
