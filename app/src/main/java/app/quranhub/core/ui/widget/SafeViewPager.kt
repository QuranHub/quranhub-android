package app.quranhub.core.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * A [ViewPager] that survives its known multi-touch bug.
 *
 * On some pointer-up sequences (seen on Xiaomi/MIUI devices), ViewPager's active pointer id is
 * no longer in the event, so `findPointerIndex` returns -1 and `MotionEvent.getX(-1)` throws
 * `IllegalArgumentException: invalid pointerIndex -1`. Dropping that one event is harmless; the
 * next DOWN/MOVE resets ViewPager's drag state.
 *
 * Use this instead of [ViewPager] in layouts until the pagers move to ViewPager2.
 */
class SafeViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ViewPager(context, attrs) {

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean =
        try {
            super.onInterceptTouchEvent(ev)
        } catch (_: IllegalArgumentException) {
            false
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean =
        try {
            super.onTouchEvent(ev)
        } catch (_: IllegalArgumentException) {
            false
        }
}
