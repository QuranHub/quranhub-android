package app.quranhub.ui.mushaf.interactor

import app.quranhub.data.Constants
import app.quranhub.data.local.entity.HizbQuarter
import app.quranhub.ui.mushaf.model.SearchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object HizbQuarterSearchMapper {

    suspend fun attachHizbQuarters(
        searchModels: List<SearchModel>,
        hizbQuarters: List<HizbQuarter>
    ): List<SearchModel> = withContext(Dispatchers.Default) {
        val ayaHezbQuarterIndex = IntArray(Constants.Quran.NUM_OF_VERSES + 1)
        for (hizbQuarter in hizbQuarters) {
            for (i in hizbQuarter.ayaFrom..hizbQuarter.ayaTo) {
                ayaHezbQuarterIndex[i] = hizbQuarter.id
            }
        }
        for (searchModel in searchModels) {
            val hezbQuarterData = ayaHezbQuarterIndex[searchModel.id]
            searchModel.hezb = (hezbQuarterData - 1) / 4 % 2 + 1
            searchModel.quarter = (hezbQuarterData - 1) % 4 + 1
        }
        searchModels
    }
}
