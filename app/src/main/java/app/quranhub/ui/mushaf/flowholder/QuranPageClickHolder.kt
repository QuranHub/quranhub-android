package app.quranhub.ui.mushaf.flowholder

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Typed flow holder for mushaf page clicks, replacing the former greenrobot
 * EventBus `QuranPageClickEvent`. Emits every time the user taps the mushaf
 * page image; consumers (currently the mushaf screen) toggle the chrome bars.
 */
object QuranPageClickHolder {

    private val _pageClicks = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val pageClicks: SharedFlow<Unit> = _pageClicks.asSharedFlow()

    fun onPageClicked() {
        _pageClicks.tryEmit(Unit)
    }
}
