package app.quranhub.feature.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.quranhub.core.common.flowholder.AudioPlaybackStateHolder
import app.quranhub.core.common.flowholder.AudioPlaybackUpdate
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes the audio playback state published by [app.quranhub.feature.mushaf.audio_manager.AyaAudioService]
 * through the typed [AudioPlaybackStateHolder] — the single source of truth
 * for playback state, replacing the former duplicate MediaPlayer-callback
 * LiveData channel.
 */
class AyaAudioViewModel(application: Application) : AndroidViewModel(application) {

    val playbackState: StateFlow<AudioPlaybackUpdate> = AudioPlaybackStateHolder.state
}
