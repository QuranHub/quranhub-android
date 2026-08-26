package app.quranhub.ui.mushaf.interactor

import androidx.lifecycle.LiveData
import app.quranhub.ui.mushaf.model.SearchModel

interface SearchInteractor {
    fun searchAya(inputQuery: String?)
    fun searchAyaInGuz(inputQuery: String?, guzNumber: Int)
    fun searchAyaInSura(inputQuery: String?, suraNumber: Int)
    fun getSurasInChapter(chapter: Int): LiveData<List<Int>>
    fun searchWithSuraAndJuz(inputSearch: String?, selectedSura: Int, selectedJuz: Int)
    fun searchWithSuraAndJuzAndHizb(inputSearch: String?, selectedSura: Int, selectedJuz: Int, selectedHezb: Int)
    fun searchWithSuraAndJuzAndHizbQuarter(inputSearch: String?, selectedSura: Int, selectedJuz: Int, selectedHezb: Int, selectedQuarter: Int)

    interface TopicListener {
        fun onGetTopics(searchModels: List<SearchModel>)
    }
}
