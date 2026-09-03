package app.quranhub.ui.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.data.Constants
import app.quranhub.data.local.db.UserDatabase
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.data.model.ReciterModel
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

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface SettingsEvent {
        data class AppLanguageChanged(val langCode: String) : SettingsEvent
    }

    data class SettingsUiState(
        val appLangIndex: Int = -1,
        val appLangName: String? = null,
        val translationLangIndex: Int = -1,
        val translationLangName: String? = null,
        val screenReadingBacklight: Boolean = false,
        val lastReadPage: Boolean = false,
        val recitationIndex: Int = 0,
        val recitationName: String? = null,
        val reciterId: String? = null,
        val reciterName: String? = null,
    )

    private val context: Application = application

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events: Flow<SettingsEvent> = _events.receiveAsFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                val appLangIndex = Constants.Language.CODES.indexOf(
                    AppPreferencesManager.getAppLangSetting(context)
                )
                val translationLangIndex = Constants.Language.CODES.indexOf(
                    AppPreferencesManager.getQuranTranslationLanguage(context)
                )
                val recitationIndex = AppPreferencesManager.getRecitationSetting(context)
                SettingsUiState(
                    appLangIndex = appLangIndex,
                    appLangName = localizedLanguageName(appLangIndex),
                    translationLangIndex = translationLangIndex,
                    translationLangName = localizedLanguageName(translationLangIndex),
                    screenReadingBacklight =
                    AppPreferencesManager.getScreenReadingBacklightSetting(context),
                    lastReadPage = AppPreferencesManager.getLastReadPageSetting(context),
                    recitationIndex = recitationIndex,
                    recitationName = localizedRecitationName(recitationIndex),
                    reciterId = AppPreferencesManager.getReciterSheikhSetting(context),
                )
            }
            _uiState.value = loaded
            loadReciterName()
        }
    }

    private suspend fun loadReciterName() {
        val reciterId = _uiState.value.reciterId ?: return
        val reciterName = withContext(Dispatchers.IO) {
            try {
                UserDatabase.getInstance(context)
                    .reciterDao
                    .getById(reciterId)
                    ?.name
            } catch (e: Exception) {
                Log.e(TAG, "loadReciterName: ", e)
                null
            }
        }
        _uiState.update { it.copy(reciterName = reciterName) }
    }

    fun onAppLanguageSelected(itemIndex: Int) {
        if (itemIndex == _uiState.value.appLangIndex) return
        val langCode = Constants.Language.CODES[itemIndex]
        AppPreferencesManager.persistAppLangSetting(context, langCode)
        _uiState.update {
            it.copy(
                appLangIndex = itemIndex,
                appLangName = localizedLanguageName(itemIndex)
            )
        }
        viewModelScope.launch {
            _events.send(SettingsEvent.AppLanguageChanged(langCode))
        }
    }

    fun onTranslationLanguageSelected(itemIndex: Int) {
        val langCode = Constants.Language.CODES[itemIndex]
        AppPreferencesManager.persistQuranTranslationLanguage(context, langCode)
        _uiState.update {
            it.copy(
                translationLangIndex = itemIndex,
                translationLangName = localizedLanguageName(itemIndex)
            )
        }
    }

    fun onScreenReadingBacklightChanged(checked: Boolean) {
        AppPreferencesManager.persistScreenReadingBacklightSetting(context, checked)
        _uiState.update { it.copy(screenReadingBacklight = checked) }
    }

    fun onLastReadPageChanged(checked: Boolean) {
        AppPreferencesManager.persistLastReadPageSetting(context, checked)
        _uiState.update { it.copy(lastReadPage = checked) }
    }

    fun onRecitationSelected(itemIndex: Int) {
        val isChanged = AppPreferencesManager.persistRecitationSetting(context, itemIndex)
        if (isChanged) {
            // update the current recitation setting & reset quran reader setting
            _uiState.update {
                it.copy(
                    recitationIndex = itemIndex,
                    recitationName = localizedRecitationName(itemIndex),
                    reciterId = null,
                    reciterName = null,
                )
            }
        }
    }

    fun onReciterSelected(reciter: ReciterModel) {
        Log.d(TAG, "onReciterSelected: reciterId=" + reciter.id)
        AppPreferencesManager.persistReciterSheikhSetting(context, reciter.id)
        _uiState.update {
            it.copy(reciterId = reciter.id, reciterName = reciter.getLocalizedName(context))
        }
    }

    private fun localizedLanguageName(index: Int): String? =
        Constants.Language.NAMES_STR_IDS.getOrNull(index)?.let { context.getString(it) }

    private fun localizedRecitationName(index: Int): String? =
        Constants.Recitation.NAMES_STR_IDS.getOrNull(index)?.let { context.getString(it) }

    companion object {
        private val TAG = SettingsViewModel::class.java.simpleName
    }
}
