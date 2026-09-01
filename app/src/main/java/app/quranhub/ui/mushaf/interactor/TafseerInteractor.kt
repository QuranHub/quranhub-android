package app.quranhub.ui.mushaf.interactor

import app.quranhub.data.local.entity.Translation
import app.quranhub.ui.mushaf.model.TafseerModel
import kotlinx.coroutines.flow.Flow

interface TafseerInteractor {
    suspend fun initTranslationDB(dbName: String?)
    fun getSuraTafseers(suraNumber: Int): Flow<List<TafseerModel>>
    fun getSuraBookTafseers(suraNumber: Int): Flow<List<Translation>>
}
