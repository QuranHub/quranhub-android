package app.quranhub.ui.mushaf.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import app.quranhub.ui.mushaf.flowholder.AudioPlaybackStateHolder
import app.quranhub.ui.mushaf.flowholder.AudioPlaybackUpdate
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes the audio playback state published by [app.quranhub.ui.mushaf.audio_manager.AyaAudioService]
 * through the typed [AudioPlaybackStateHolder] — the single source of truth
 * for playback state, replacing the former duplicate MediaPlayer-callback
 * LiveData channel.
 */
class AyaAudioViewModel(application: Application) : AndroidViewModel(application) {

    val playbackState: StateFlow<AudioPlaybackUpdate> = AudioPlaybackStateHolder.state
}
