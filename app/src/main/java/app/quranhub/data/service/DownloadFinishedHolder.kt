package app.quranhub.data.service

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Typed flow holder for download-finished notifications — the single source
 * of truth between the downloader services and their UI consumers, replacing
 * the former greenrobot `DownloadFinishEvent` EventBus stream.
 *
 * Carries no replay: consumers only observe completions that happen while
 * they are started, like the fire-and-forget EventBus contract this replaces.
 *
 * [notifyFinished] is written only from the services' main thread; consumers
 * collect on their own lifecycle scopes.
 */
object DownloadFinishedHolder {

    private val _finished = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )

    /** Emits once whenever a downloader service finishes all its downloads. */
    val finished: SharedFlow<Unit> = _finished

    fun notifyFinished() {
        _finished.tryEmit(Unit)
    }
}
