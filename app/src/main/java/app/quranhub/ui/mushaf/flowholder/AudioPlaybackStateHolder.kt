package app.quranhub.ui.mushaf.flowholder

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The mushaf audio playback states, previously carried by the greenrobot
 * `AudioStateEvent` posted from [app.quranhub.ui.mushaf.audio_manager.AyaAudioService].
 */
enum class AudioPlaybackState {
    PLAYING,
    PAUSED,
    RESUMED,
    STOPPED,
    COMPLETED,
    PLAY_NEXT,
    PLAY_PREV,
    NOT_DOWNLOADED,
    GROUP_REPEAT_COMPLETED
}

/**
 * One playback-state update emitted by the audio service.
 *
 * [seq] monotonically increases per update so that repeating the same
 * [state] (e.g. a second NOT_DOWNLOADED for a missing audio file) still
 * reaches collectors, which StateFlow's conflation of equal values would
 * otherwise drop.
 */
data class AudioPlaybackUpdate(
    val state: AudioPlaybackState,
    val seq: Long
)

/**
 * Typed flow holder for audio playback state — the single source of truth
 * between [app.quranhub.ui.mushaf.audio_manager.AyaAudioService] and its
 * UI consumers, replacing the former greenrobot EventBus audio stream.
 *
 * [update] is written only from the audio service's main thread; consumers
 * collect on their own lifecycle scopes.
 */
object AudioPlaybackStateHolder {

    private var seq = 0L

    private val _state = MutableStateFlow(AudioPlaybackUpdate(AudioPlaybackState.STOPPED, seq))
    val state: StateFlow<AudioPlaybackUpdate> = _state.asStateFlow()

    fun update(state: AudioPlaybackState) {
        _state.value = AudioPlaybackUpdate(state, ++seq)
    }

    @VisibleForTesting
    fun reset() {
        seq = 0
        _state.value = AudioPlaybackUpdate(AudioPlaybackState.STOPPED, seq)
    }
}
