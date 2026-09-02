package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.R
import app.quranhub.ui.mushaf.interactor.SearchInteractor
import app.quranhub.ui.mushaf.interactor.SearchInteractorImp
import app.quranhub.ui.mushaf.model.SearchModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface SearchEvent {
        data class ShowError(val message: String) : SearchEvent
    }

    data class SearchUiState(
        val loading: Boolean = false,
        val results: List<SearchModel>? = null
    )

    private val context: Application = application
    private val interactor: SearchInteractor = SearchInteractorImp(application)

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _suras = MutableStateFlow<List<Int>?>(null)
    val suras: StateFlow<List<Int>?> = _suras.asStateFlow()

    private val _events = Channel<SearchEvent>(Channel.BUFFERED)
    val events: Flow<SearchEvent> = _events.receiveAsFlow()

    private var surasJob: Job? = null
    private var searchJob: Job? = null

    fun simpleSearch(input: String) = search { interactor.searchAya(input) }

    fun searchWithSura(input: String, suraNumber: Int) =
        search { interactor.searchAyaInSura(input, suraNumber) }

    fun searchWithJuz(input: String, juzNumber: Int) =
        search { interactor.searchAyaInGuz(input, juzNumber) }

    fun getChapterSuras(juzNumber: Int) {
        surasJob?.cancel()
        surasJob = viewModelScope.launch {
            try {
                interactor.getSurasInChapter(juzNumber).collect { suraNumbers ->
                    _suras.value = suraNumbers
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(SearchEvent.ShowError(context.getString(R.string.search_failed)))
            }
        }
    }

    fun searchWithSuraAndJuz(input: String, selectedSura: Int, selectedJuz: Int) =
        search { interactor.searchWithSuraAndJuz(input, selectedSura, selectedJuz) }

    fun searchWithSuraAndJuzAndHizb(
        input: String,
        selectedSura: Int,
        selectedJuz: Int,
        selectedHezb: Int
    ) = search { interactor.searchWithSuraAndJuzAndHizb(input, selectedSura, selectedJuz, selectedHezb) }

    fun searchWithSuraAndJuzAndHizbQuarter(
        input: String,
        selectedSura: Int,
        selectedJuz: Int,
        selectedHezb: Int,
        selectedQuarter: Int
    ) = search {
        interactor.searchWithSuraAndJuzAndHizbQuarter(
            input,
            selectedSura,
            selectedJuz,
            selectedHezb,
            selectedQuarter
        )
    }

    private fun search(searchQuery: suspend () -> List<SearchModel>) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState(loading = true)
            try {
                val results = searchQuery()
                _uiState.value = SearchUiState(loading = false, results = results)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = SearchUiState(loading = false)
                _events.send(SearchEvent.ShowError(context.getString(R.string.search_failed)))
            }
        }
    }
}
