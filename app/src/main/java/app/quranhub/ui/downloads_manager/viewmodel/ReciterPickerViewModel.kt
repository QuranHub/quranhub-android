package app.quranhub.ui.downloads_manager.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.data.Constants
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.entity.Reciter
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.data.model.ReciterModel
import app.quranhub.data.repository.RecitationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Quran reciter picker dialog: loads the available reciters
 * for a recitation (remote Firestore with a local-database fallback), tracks
 * the user's selection and persists it on select.
 */
class ReciterPickerViewModel(
    application: Application,
    private val recitationId: Int,
    private val selectedReciterId: String?
) : AndroidViewModel(application) {

    sealed interface ReciterPickerEvent {
        data object NoReciters : ReciterPickerEvent
        data class ReciterSelected(val recitationId: Int, val reciterModel: ReciterModel) :
            ReciterPickerEvent
    }

    data class ReciterPickerUiState(
        val loading: Boolean = true,
        val reciterNames: List<String> = emptyList(),
        val selectedReciterIndex: Int = -1,
        val isSelectEnabled: Boolean = false,
        val showDownloadedOnlyMessage: Boolean = false,
        val showNoInternetMessage: Boolean = false
    )

    private val appContext = application

    private val recitationsRepository = RecitationsRepository()

    private var reciterModels: List<ReciterModel> = emptyList()

    private val _uiState = MutableStateFlow(ReciterPickerUiState())
    val uiState: StateFlow<ReciterPickerUiState> = _uiState.asStateFlow()

    private val _events = Channel<ReciterPickerEvent>(Channel.BUFFERED)
    val events: Flow<ReciterPickerEvent> = _events.receiveAsFlow()

    init {
        loadReciters()
    }

    fun loadReciters() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    loading = true,
                    showDownloadedOnlyMessage = false,
                    showNoInternetMessage = false
                )
            }

            val recitationKey: String = when (recitationId) {
                Constants.Recitation.HAFS_ID -> Constants.Recitation.HAFS_KEY
                Constants.Recitation.WARSH_ID -> Constants.Recitation.WARSH_KEY
                else -> error("Invalid recitation id: $recitationId")
            }

            try {
                val reciters =
                    withContext(Dispatchers.IO) {
                        recitationsRepository.getRecitersForRecitation(recitationKey)
                    }
                reciterModels = reciters
                if (reciters.isNotEmpty()) {
                    val preselectedIndex = reciters.indexOfFirst { it.id == selectedReciterId }
                    _uiState.update {
                        it.copy(
                            loading = false,
                            reciterNames = reciters.map { model ->
                                model.getLocalizedName(appContext)
                            },
                            selectedReciterIndex = preselectedIndex,
                            isSelectEnabled = preselectedIndex != -1
                        )
                    }
                } else {
                    Log.e(TAG, "The fetched reciters list is empty!")
                    _uiState.update { it.copy(loading = false, reciterNames = emptyList()) }
                    _events.send(ReciterPickerEvent.NoReciters)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching reciters", e)

                // try to display the downloaded reciters
                // TODO load from DB only for a selected aya, or page or sura, to guarantee reciter is downloaded
                val localReciters = withContext(Dispatchers.IO) { loadRecitersFromDb() }
                reciterModels = localReciters
                _uiState.update { state ->
                    state.copy(
                        loading = false,
                        reciterNames = localReciters.map { model ->
                            model.getLocalizedName(appContext)
                        },
                        selectedReciterIndex = localReciters.indexOfFirst {
                            it.id == selectedReciterId
                        },
                        showDownloadedOnlyMessage = localReciters.isNotEmpty(),
                        showNoInternetMessage = localReciters.isEmpty()
                    )
                }
            }
        }
    }

    private fun loadRecitersFromDb(): List<ReciterModel> {
        val recitersList = UserDatabase.getInstance(appContext)
            .reciterDao
            .getAllForRecitation(recitationId)

        // Convert from Reciter to ReciterModel
        return recitersList.map { r ->
            ReciterModel(
                id = r.id,
                localizedName = r.name,
                localizedNationality = r.nationality,
                audioBaseUrl = r.audioBaseUrl
            )
        }
    }

    fun onReciterClicked(index: Int) {
        _uiState.update {
            it.copy(selectedReciterIndex = index, isSelectEnabled = true)
        }
    }

    fun onSelectClicked() {
        val state = _uiState.value
        if (state.selectedReciterIndex == -1) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSelectEnabled = false) }
            try {
                val selectedReciterModel = reciterModels[state.selectedReciterIndex]
                withContext(Dispatchers.IO) {
                    // Store selected reciter in DB
                    val userDatabase = UserDatabase.getInstance(appContext)
                    if (userDatabase.reciterDao.getById(selectedReciterModel.id) == null) {
                        userDatabase.reciterDao.insert(
                            Reciter(
                                selectedReciterModel.id,
                                selectedReciterModel.getLocalizedName(appContext),
                                selectedReciterModel.getLocalizedNationality(appContext),
                                selectedReciterModel.audioBaseUrl
                            )
                        )
                    }

                    // persist selected reciter as preference if recitation id matches the one in preferences
                    val recitationIdPreference =
                        AppPreferencesManager.getRecitationSetting(appContext)
                    if (recitationIdPreference == recitationId) {
                        AppPreferencesManager.persistReciterSheikhSetting(
                            appContext,
                            selectedReciterModel.id
                        )
                    }
                }
                _events.send(
                    ReciterPickerEvent.ReciterSelected(recitationId, selectedReciterModel)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist selected reciter", e)
                _uiState.update { it.copy(isSelectEnabled = true) }
            }
        }
    }

    companion object {
        private val TAG = ReciterPickerViewModel::class.java.simpleName
    }
}
