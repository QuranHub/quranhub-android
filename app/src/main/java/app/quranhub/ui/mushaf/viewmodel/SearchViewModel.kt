package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import app.quranhub.ui.mushaf.interactor.SearchInteractor
import app.quranhub.ui.mushaf.interactor.SearchInteractorImp
import app.quranhub.ui.mushaf.model.SearchModel

class SearchViewModel(application: Application) : AndroidViewModel(application),
    SearchInteractor.TopicListener {

    private val interactor: SearchInteractor
    private var suraLiveData: LiveData<List<Int>>? = null
    val sura: MediatorLiveData<List<Int>>
    val search: MediatorLiveData<List<SearchModel>>

    init {
        interactor = SearchInteractorImp(application, this)
        sura = MediatorLiveData()
        search = MediatorLiveData()
    }

    fun simpleSearch(input: String?) {
        interactor.searchAya(input)
    }

    fun searchWithSura(input: String?, suraNumber: Int) {
        interactor.searchAyaInSura(input, suraNumber)
    }

    fun searchWithJuz(input: String?, juzNumber: Int) {
        interactor.searchAyaInGuz(input, juzNumber)
    }

    fun getChapterSuras(juzNumber: Int) {
        suraLiveData = interactor.getSurasInChapter(juzNumber)
        sura.addSource(suraLiveData!!) { tafseerModels: List<Int> ->
            sura.value = tafseerModels
            sura.removeSource(suraLiveData!!)
        }
    }

    fun searchWithSuraAndJuz(inputSearch: String?, selectedSura: Int, selectedJuz: Int) {
        interactor.searchWithSuraAndJuz(inputSearch, selectedSura, selectedJuz)
    }

    fun searchWithSuraAndJuzAndHizbQuarter(
        inputSearch: String?,
        selectedSura: Int,
        selectedJuz: Int,
        selectedHezb: Int,
        selectedQuarter: Int
    ) {
        interactor.searchWithSuraAndJuzAndHizbQuarter(
            inputSearch,
            selectedSura,
            selectedJuz,
            selectedHezb,
            selectedQuarter
        )
    }

    fun searchWithSuraAndJuzAndHizb(
        inputSearch: String?,
        selectedSura: Int,
        selectedJuz: Int,
        selectedHezb: Int
    ) {
        interactor.searchWithSuraAndJuzAndHizb(inputSearch, selectedSura, selectedJuz, selectedHezb)
    }

    override fun onGetTopics(searchModels: List<SearchModel>) {
        search.value = searchModels
    }
}