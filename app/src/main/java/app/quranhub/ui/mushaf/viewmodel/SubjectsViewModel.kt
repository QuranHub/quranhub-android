package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.ui.mushaf.interactor.SubjectInteractor
import app.quranhub.ui.mushaf.interactor.SubjectInteractorImp
import app.quranhub.ui.mushaf.model.TopicModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubjectsViewModel(application: Application) : AndroidViewModel(application) {

    data class SubjectsUiState(
        val loading: Boolean = true,
        val subjects: List<TopicModel>? = null
    )

    private val interactor: SubjectInteractor = SubjectInteractorImp(application)

    private val _uiState = MutableStateFlow(SubjectsUiState())
    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()

    fun getSubjects(subjects: List<String>, subjectsCategory: List<String>) {
        if (_uiState.value.subjects != null) {
            return
        }
        viewModelScope.launch {
            _uiState.value = SubjectsUiState(loading = true)
            try {
                val topicModels = interactor.getSubjects(subjects, subjectsCategory)
                _uiState.value = SubjectsUiState(loading = false, subjects = topicModels)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = SubjectsUiState(loading = false)
            }
        }
    }
}
