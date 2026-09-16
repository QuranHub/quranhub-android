package app.quranhub.feature.mushaf.interactor

import app.quranhub.core.data.local.entity.Translation
import app.quranhub.core.data.model.TafseerModel
import kotlinx.coroutines.flow.Flow

interface TafseerInteractor {
    suspend fun initTranslationDB(dbName: String?)
    fun getSuraTafseers(suraNumber: Int): Flow<List<TafseerModel>>
    fun getSuraBookTafseers(suraNumber: Int): Flow<List<Translation>>
}
