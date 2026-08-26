package app.quranhub.ui.mushaf.interactor

import androidx.lifecycle.LiveData
import app.quranhub.data.local.entity.Translation
import app.quranhub.ui.mushaf.model.TafseerModel

interface TafseerInteractor {
    fun getSuraTafseers(suraNumber: Int): LiveData<List<TafseerModel>>
    fun initTranslationDB(dbName: String?)
    fun getSuraBookTafseers(suraNumber: Int): LiveData<List<Translation>>
    fun getSuraAyah(suraNumber: Int): LiveData<List<TafseerModel>>
}
