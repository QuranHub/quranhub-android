package app.quranhub.util

import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable

object ImageUtil {

    /**
     * Color matrix that flips the components (`-1.0f * c + 255 = 255 - c`)
     * and keeps the alpha intact.
     */
    private val NEGATIVE by lazy {
        floatArrayOf(
            -1.0f, 0f, 0f, 0f, 255f,  // red
            0f, -1.0f, 0f, 0f, 255f,  // green
            0f, 0f, -1.0f, 0f, 255f,  // blue
            0f, 0f, 0f, 1.0f, 0f      // alpha
        )
    }

    /**
     * Invert the colors for the given drawable.
     *
     * @param drawable
     */
    fun invertDrawable(drawable: Drawable) {
        drawable.colorFilter = ColorMatrixColorFilter(NEGATIVE)
    }
}