package app.quranhub.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.quranhub.data.Constants
import app.quranhub.data.local.prefs.AppPreferencesManager
import app.quranhub.ui.mushaf.audio_manager.AyaAudioService
import app.quranhub.util.SharedPrefsUtils.getBoolean
import app.quranhub.util.SharedPrefsUtils.getInteger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    sealed interface LaunchDestination {
        data class Notification(val ayaId: Int) : LaunchDestination
        data class LastReadPage(val pageNumber: Int) : LaunchDestination
        data object Mushaf : LaunchDestination
    }

    private val _launchEvents = Channel<LaunchDestination>(Channel.BUFFERED)
    val launchEvents: Flow<LaunchDestination> = _launchEvents.receiveAsFlow()

    fun computeLaunchDestination(fromNotification: Boolean) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            _launchEvents.send(
                when {
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
            )
        }
    }
}
