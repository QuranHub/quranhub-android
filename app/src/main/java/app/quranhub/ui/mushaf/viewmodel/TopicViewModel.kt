package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.ui.mushaf.interactor.TopicInteractor
import app.quranhub.ui.mushaf.interactor.TopicInteractorImp
import app.quranhub.ui.mushaf.model.SearchModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TopicViewModel(application: Application) : AndroidViewModel(application) {

    data class TopicAyasUiState(
        val loading: Boolean = true,
        val ayahs: List<SearchModel>? = null
    )

    private val interactor: TopicInteractor = TopicInteractorImp(application)

    private val _uiState = MutableStateFlow(TopicAyasUiState())
    val uiState: StateFlow<TopicAyasUiState> = _uiState.asStateFlow()

    private var loadedCategoryId = -1

    fun getAyas(categoryId: Int) {
        if (categoryId == loadedCategoryId && _uiState.value.ayahs != null) {
            return
        }
        viewModelScope.launch {
            _uiState.value = TopicAyasUiState(loading = true)
            try {
                val ayahs = interactor.getAyas(categoryId)
                loadedCategoryId = categoryId
                _uiState.value = TopicAyasUiState(loading = false, ayahs = ayahs)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = TopicAyasUiState(loading = false)
            }
        }
    }
}
