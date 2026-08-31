package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.ui.mushaf.interactor.Guz2IndexInteractor
import app.quranhub.ui.mushaf.interactor.Guz2IndexInteractorImp
import app.quranhub.ui.mushaf.model.HizbQuarterDataModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class Guz2IndexViewModel(application: Application) : AndroidViewModel(application) {

    data class JuzIndexUiState(
        val loading: Boolean = true,
        val items: List<HizbQuarterDataModel> = emptyList()
    )

    class IndexItemClickEvent(val page: Int)

    private val guz2IndexInteractor: Guz2IndexInteractor = Guz2IndexInteractorImp(application)

    private val _uiState = MutableStateFlow(JuzIndexUiState())
    val uiState: StateFlow<JuzIndexUiState> = _uiState.asStateFlow()

    private val _indexItemClickEvents = Channel<IndexItemClickEvent>(Channel.BUFFERED)
    val indexItemClickEvents: Flow<IndexItemClickEvent> = _indexItemClickEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            guz2IndexInteractor.allHizbQuarterDataModel.collect { hizbQuarterDataModels ->
                _uiState.update { it.copy(loading = false, items = hizbQuarterDataModels) }
            }
        }
    }

    fun notifyIndexItemClick(clickedItemIndex: Int) {
        val startPage = _uiState.value.items[clickedItemIndex].startPage
        viewModelScope.launch {
            _indexItemClickEvents.send(IndexItemClickEvent(startPage))
        }
    }
}
