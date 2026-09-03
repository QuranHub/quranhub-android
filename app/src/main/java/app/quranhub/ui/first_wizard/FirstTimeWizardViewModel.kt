package app.quranhub.ui.first_wizard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.data.Constants
import app.quranhub.data.local.db.MushafDatabase
import app.quranhub.data.local.db.RoomAsset
import app.quranhub.data.local.prefs.AppPreferencesManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FirstTimeWizardViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface WizardEvent {
        data class AppLanguageChanged(val langCode: String) : WizardEvent
        data object WizardFinished : WizardEvent
    }

    data class WizardUiState(
        val appLangIndex: Int = -1,
        val translationLangIndex: Int = -1,
        val recitationIndex: Int = 0,
    )

    private val context: Application = application

    private val _uiState = MutableStateFlow(WizardUiState())
    val uiState: StateFlow<WizardUiState> = _uiState.asStateFlow()

    /**
     * The logical step the wizard is currently showing
     * (0: app language, 1: translation languages, 2: recitations),
     * preserved across configuration changes.
     */
    private val _currentStep = MutableStateFlow(FIRST_STEP)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    /** The current options-list search query, preserved across configuration changes. */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _events = Channel<WizardEvent>(Channel.BUFFERED)
    val events: Flow<WizardEvent> = _events.receiveAsFlow()

    init {
        // Initialize Mus'haf metadata DB. This runs once per wizard instance
        // (the ViewModel survives configuration changes) and RoomAsset itself
        // short-circuits if the DB is already up to date.
        RoomAsset.initializeDatabase(
            context, MushafDatabase.DATABASE_NAME, MushafDatabase.ASSET_DB_VERSION
        )
        loadSelectedOptions()
    }

    private fun loadSelectedOptions() {
        _uiState.value = WizardUiState(
            appLangIndex = Constants.Language.CODES.indexOf(
                AppPreferencesManager.getAppLangSetting(context)
            ),
            translationLangIndex = Constants.Language.CODES.indexOf(
                AppPreferencesManager.getQuranTranslationLanguage(context)
            ),
            recitationIndex = AppPreferencesManager.getRecitationSetting(context),
        )
    }

    fun onStepSelected(step: Int) {
        if (_currentStep.value != step) {
            _currentStep.value = step
            // Navigating to a different step resets the search.
            _searchQuery.value = ""
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onNextClicked() {
        if (_currentStep.value < LAST_STEP) {
            onStepSelected(_currentStep.value + 1)
        } else {
            finishWizard()
        }
    }

    fun onBackClicked() {
        if (_currentStep.value > FIRST_STEP) {
            onStepSelected(_currentStep.value - 1)
        }
    }

    /**
     * Navigate to main activity and mark wizard as done.
     */
    private fun finishWizard() {
        AppPreferencesManager.markFirstTimeWizardDone(context)
        viewModelScope.launch {
            _events.send(WizardEvent.WizardFinished)
        }
    }

    fun onAppLanguageSelected(itemIndex: Int) {
        val selectedLangCode = Constants.Language.CODES[itemIndex]
        if (selectedLangCode != AppPreferencesManager.getAppLangSetting(context)) {
            AppPreferencesManager.persistAppLangSetting(context, selectedLangCode)
            _uiState.update { it.copy(appLangIndex = itemIndex) }
            viewModelScope.launch {
                _events.send(WizardEvent.AppLanguageChanged(selectedLangCode))
            }
        }
    }

    fun onTranslationLanguageSelected(itemIndex: Int) {
        AppPreferencesManager.persistQuranTranslationLanguage(
            context, Constants.Language.CODES[itemIndex]
        )
        _uiState.update { it.copy(translationLangIndex = itemIndex) }
    }

    fun onRecitationSelected(itemIndex: Int) {
        AppPreferencesManager.persistRecitationSetting(context, itemIndex)
        _uiState.update { it.copy(recitationIndex = itemIndex) }
    }

    companion object {
        const val FIRST_STEP = 0 // app language step
        const val TRANSLATIONS_STEP = 1 // translation languages step
        const val LAST_STEP = 2 // recitations step
    }
}
