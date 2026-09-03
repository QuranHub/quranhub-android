package app.quranhub.ui.mushaf.interactor

import app.quranhub.data.local.entity.Aya
import app.quranhub.data.local.entity.TranslationBook
import app.quranhub.ui.mushaf.model.QuranPageInfo
import app.quranhub.ui.mushaf.model.SuraVersesNumber

interface Mus7fInteractor {
    suspend fun getPageInfo(currentPage: Int): QuranPageInfo?
    suspend fun getAyaTafseer(ayaId: Int): String?
    suspend fun getTafseerBook(currentTafsserId: String): TranslationBook?
    fun initTranslationDB(dbName: String)
    suspend fun getPageSuras(): ArrayList<ArrayList<Int>>
    suspend fun checkAyaHasRecorder(id: Int): String?
    suspend fun saveRecorderPath(ayaId: Int, recorderPath: String)
    suspend fun deleteAyaVoiceRecorder(ayaId: Int)
    suspend fun getSuraNumofVerses(): ArrayList<SuraVersesNumber>
    suspend fun getFromAyaPage(fromAya: Int): Int?
    suspend fun getAya(currentAyaId: Int): Aya?
}
