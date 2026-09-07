package app.quranhub.util

import kotlin.math.roundToInt

/**
 * Quran page zoom scale arithmetic.
 *
 * Zoom values are persisted and re-applied across app launches, so they must be
 * free of floating-point drift: incrementing/decrementing raw floats
 * (e.g. `1.05f - 0.05f == 0.99999994f`) would otherwise prevent the scale from
 * ever returning to exactly 1x. All operations round to two decimal places and
 * clamp to the valid scale range.
 */
object QuranPageZoom {

    const val MIN_SCALE = 1f
    const val MAX_SCALE = 1.5f
    const val SCALE_INCREMENT = 0.05f

    private const val SCALE_PRECISION = 100

    fun normalize(scale: Float): Float {
        val clamped = scale.coerceIn(MIN_SCALE, MAX_SCALE)
        return (clamped * SCALE_PRECISION).roundToInt() / SCALE_PRECISION.toFloat()
    }

    fun zoomIn(currentScale: Float): Float {
        return normalize(currentScale + SCALE_INCREMENT)
    }

    fun zoomOut(currentScale: Float): Float {
        return normalize(currentScale - SCALE_INCREMENT)
    }

    fun canZoomIn(currentScale: Float): Boolean {
        return currentScale < MAX_SCALE
    }

    fun canZoomOut(currentScale: Float): Boolean {
        return currentScale > MIN_SCALE
    }
}
