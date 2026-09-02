package app.quranhub.ui.mushaf.interactor

import android.content.Context
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.ui.mushaf.model.SearchModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

class SearchInteractorImp(context: Context) : SearchInteractor {

    private val mushafDatabase: MushafDatabase =
        MushafDatabase.getInstance(context.applicationContext)

    override suspend fun searchAya(inputQuery: String): List<SearchModel> =
        searchAndAttachHizbQuarters { mushafDatabase.ayaDao.getSimpleSearchResult(inputQuery) }

    override suspend fun searchAyaInGuz(inputQuery: String, guzNumber: Int): List<SearchModel> =
        searchAndAttachHizbQuarters { mushafDatabase.ayaDao.getJuzSearchResult(inputQuery, guzNumber) }

    override suspend fun searchAyaInSura(inputQuery: String, suraNumber: Int): List<SearchModel> =
        searchAndAttachHizbQuarters { mushafDatabase.ayaDao.getSuraSearchResult(inputQuery, suraNumber) }

    override fun getSurasInChapter(chapter: Int): Flow<List<Int>> =
        mushafDatabase.ayaDao.getSurasInChapter(chapter)

    override suspend fun searchWithSuraAndJuz(
        inputSearch: String,
        selectedSura: Int,
        selectedJuz: Int
    ): List<SearchModel> = searchAndAttachHizbQuarters {
        mushafDatabase.ayaDao.getSuraJuzSearchResult(inputSearch, selectedSura, selectedJuz)
    }

    override suspend fun searchWithSuraAndJuzAndHizb(
        inputSearch: String,
        selectedSura: Int,
        selectedJuz: Int,
        selectedHezb: Int
    ): List<SearchModel> {
        val startHezbInterval = hezbIntervalStart(selectedJuz, selectedHezb)
        val endHezbInterval = startHezbInterval + 3
        return searchAndAttachHizbQuarters {
            if (selectedSura == 0) {
                mushafDatabase.ayaDao.getJuzHezbSearchResult(
                    inputSearch,
                    selectedJuz,
                    startHezbInterval,
                    endHezbInterval
                )
            } else {
                mushafDatabase.ayaDao.getSuraJuzHezbSearchResult(
                    inputSearch,
                    selectedSura,
                    selectedJuz,
                    startHezbInterval,
                    endHezbInterval
                )
            }
        }
    }

    override suspend fun searchWithSuraAndJuzAndHizbQuarter(
        inputSearch: String,
        selectedSura: Int,
        selectedJuz: Int,
        selectedHezb: Int,
        selectedQuarter: Int
    ): List<SearchModel> {
        val startHezbInterval = hezbIntervalStart(selectedJuz, selectedHezb) + (selectedQuarter - 1)
        return searchAndAttachHizbQuarters {
            if (selectedSura == 0) {
                mushafDatabase.ayaDao.getJuzHezbSearchResult(
                    inputSearch,
                    selectedJuz,
                    startHezbInterval,
                    startHezbInterval
                )
            } else {
                mushafDatabase.ayaDao.getSuraJuzHezbSearchResult(
                    inputSearch,
                    selectedSura,
                    selectedJuz,
                    startHezbInterval,
                    startHezbInterval
                )
            }
        }
    }

    private fun hezbIntervalStart(selectedJuz: Int, selectedHezb: Int): Int {
        var startHezbInterval = (selectedJuz - 1) * 8 + 1
        if (selectedHezb == 2) {
            startHezbInterval += 4
        }
        return startHezbInterval
    }

    private suspend fun searchAndAttachHizbQuarters(
        searchQuery: suspend () -> List<SearchModel>
    ): List<SearchModel> = coroutineScope {
        val searchResults = async { searchQuery() }
        val hizbQuarters = async { mushafDatabase.hizbQuarterDao.getAll() }
        HizbQuarterSearchMapper.attachHizbQuarters(searchResults.await(), hizbQuarters.await())
    }
}
