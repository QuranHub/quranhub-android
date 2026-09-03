package app.quranhub.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.data.Constants
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.ui.mushaf.audio_manager.AyaAudioService
import app.quranhub.util.SharedPrefsUtils.getBoolean
import app.quranhub.util.SharedPrefsUtils.getInteger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface LaunchDestination {
        data class Notification(val ayaId: Int) : LaunchDestination
        data class LastReadPage(val pageNumber: Int) : LaunchDestination
        data object Mushaf : LaunchDestination
    }

    private val _launchDestination = MutableStateFlow<LaunchDestination?>(null)
    val launchDestination: StateFlow<LaunchDestination?> = _launchDestination.asStateFlow()

    fun computeLaunchDestination(fromNotification: Boolean) {
        if (_launchDestination.value != null) return
        viewModelScope.launch {
            val context = getApplication<Application>()
            _launchDestination.value = when {
                fromNotification || getBoolean(
                    context, AyaAudioService.SERVICE_RUNNING, false
                ) -> LaunchDestination.Notification(
                    getInteger(context, AyaAudioService.AYA_ID_KEY, 1)
                )

                AppPreferencesManager.getLastReadPageSetting(context) -> LaunchDestination.LastReadPage(
                    Constants.Quran.NUM_OF_PAGES - getInteger(
                        context, "last_open_page", Constants.Quran.NUM_OF_PAGES - 1
                    )
                )

                else -> LaunchDestination.Mushaf
            }
        }
    }
}
