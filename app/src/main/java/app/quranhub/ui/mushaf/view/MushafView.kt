package app.quranhub.ui.mushaf.view

import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.ui.base.BaseView
import app.quranhub.ui.mushaf.model.QuranPageInfo
import app.quranhub.ui.mushaf.model.SuraVersesNumber

interface MushafView : BaseView {
    fun showQuranPageInfo(quranPageInfo: QuranPageInfo)
    fun onGetAyaTafseer(tafseer: String)
    fun onGetTafseerBook(book: TranslationBook)
    fun onNoBooksExist()
    fun onGetPageSuras(suras: ArrayList<ArrayList<Int>>)
    fun onGetAyaRecorder(recorderPath: String)
    fun onGetSuraVersesNumber(suraVersesNumbers: ArrayList<SuraVersesNumber>)
    fun onGetAyaPage(page: Int)
    fun onGetCurrentAyaFromNotification(aya: Aya)
}
