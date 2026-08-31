package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.R
import app.quranhub.ui.mushaf.interactor.SuraGuz2IndexInteractor
import app.quranhub.ui.mushaf.interactor.SuraGuz2IndexInteractorImp
import app.quranhub.ui.mushaf.model.SuraIndexModel
import app.quranhub.ui.mushaf.model.SuraIndexModelMapper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SuraIndexViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface SuraIndexEvent {
        class NavigateToSura(val page: Int) : SuraIndexEvent
        class ShowError(val message: String) : SuraIndexEvent
    }

    data class SuraIndexUiState(
        val loading: Boolean = true,
        val items: List<SuraIndexModelMapper> = emptyList()
    )

    private val context: Application = application
    private val suraGuz2IndexInteractor: SuraGuz2IndexInteractor =
        SuraGuz2IndexInteractorImp(application)

    private val _uiState = MutableStateFlow(SuraIndexUiState())
    val uiState: StateFlow<SuraIndexUiState> = _uiState.asStateFlow()

    private val _suraIndexEvents = Channel<SuraIndexEvent>(Channel.BUFFERED)
    val suraIndexEvents: Flow<SuraIndexEvent> = _suraIndexEvents.receiveAsFlow()

    init {
        loadSuraIndex()
    }

    private fun loadSuraIndex() {
        viewModelScope.launch {
            try {
                val suraIndexModels = suraGuz2IndexInteractor.getSuraIndex()
                _uiState.value = SuraIndexUiState(
                    loading = false,
                    items = suraIndexModels.map { mapToViewItem(it) }
                )
            } catch (e: Exception) {
                _uiState.value = SuraIndexUiState(loading = false)
                _suraIndexEvents.send(SuraIndexEvent.ShowError(context.getString(R.string.sura_index_failed)))
            }
        }
    }

    private fun mapToViewItem(model: SuraIndexModel): SuraIndexModelMapper {
        val localizedType = when (model.type) {
            "Medinan" -> context.getString(R.string.sura_madnya)
            "Meccan" -> context.getString(R.string.sura_makya)
            else -> model.type
        }
        return SuraIndexModelMapper.mapToString(model.copy(type = localizedType), context)
    }

    fun onSuraItemClick(suraPage: Int) {
        viewModelScope.launch {
            _suraIndexEvents.send(SuraIndexEvent.NavigateToSura(suraPage))
        }
    }
}
