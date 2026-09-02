package app.quranhub.ui.mushaf.interactor

import app.quranhub.ui.mushaf.model.SearchModel
import kotlinx.coroutines.flow.Flow

interface SearchInteractor {
    suspend fun searchAya(inputQuery: String): List<SearchModel>
    suspend fun searchAyaInGuz(inputQuery: String, guzNumber: Int): List<SearchModel>
    suspend fun searchAyaInSura(inputQuery: String, suraNumber: Int): List<SearchModel>
    fun getSurasInChapter(chapter: Int): Flow<List<Int>>
    suspend fun searchWithSuraAndJuz(inputSearch: String, selectedSura: Int, selectedJuz: Int): List<SearchModel>
    suspend fun searchWithSuraAndJuzAndHizb(inputSearch: String, selectedSura: Int, selectedJuz: Int, selectedHezb: Int): List<SearchModel>
    suspend fun searchWithSuraAndJuzAndHizbQuarter(inputSearch: String, selectedSura: Int, selectedJuz: Int, selectedHezb: Int, selectedQuarter: Int): List<SearchModel>
}
