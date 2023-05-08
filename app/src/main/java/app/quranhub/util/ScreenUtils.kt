package app.quranhub.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.core.view.ViewCompat
import app.quranhub.ui.mushaf.model.ScreenSize

object ScreenUtils {

    private val TAG = ScreenUtils::class.java.simpleName

    const val PORTRAIT_STATE = "PORTRAIT"
    const val LANDSCAPE_STATE = "LANDSCAPE"

    @JvmStatic
    fun getOrientationState(context: Context): String {
        val orientation = context.resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            PORTRAIT_STATE
        } else {
            LANDSCAPE_STATE
        }
    }

    @JvmStatic
    fun isPortrait(context: Context): Boolean {
        return getOrientationState(context) == PORTRAIT_STATE
    }

    @JvmStatic
    fun isLandscape(context: Context): Boolean {
        return getOrientationState(context) == LANDSCAPE_STATE
    }

    fun getStatusBarHeight(context: Context, quranPageIv: ImageView): Int {
        val coordOffset = IntArray(2)
        quranPageIv.getLocationOnScreen(coordOffset)
        val statusBarHeight =
            Math.ceil((25 * context.resources.displayMetrics.density).toDouble()).toInt()
        return coordOffset[1] - statusBarHeight
    }

    @JvmStatic
    fun dismissKeyboard(context: Context, view: View) {
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Gets the available screen width & height. Ignores the bottom system navigation bar (if exists).
     * Example: In PORTRAIT width:720, height:1184. In LANDSCAPE width:1184, height: 720.
     *
     * @param activity current activity reference.
     * @return ScreenSize instance holding width & height information.
     */
    fun getScreenSize(activity: Activity): ScreenSize {
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        val screenSize = ScreenSize(size.x, size.y)
        Log.d(
            TAG, "getScreenSize(): width = " + screenSize.width
                    + ", height = " + screenSize.height
        )
        return screenSize
    }

    /**
     * Control whether to keep the device's screen turned on and bright or to set it back to normal.
     *
     * @param activity current activity instance.
     * @param enable   whether to enable or disable this feature.
     */
    @JvmStatic
    fun keepScreenOn(activity: Activity, enable: Boolean) {
        if (enable) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    fun isLayoutRtl(view: View): Boolean {
        return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL
    }
}