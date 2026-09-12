package app.quranhub.util

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Applies status/navigation-bar insets as padding or margin.
 *
 * Uses platform [WindowInsets] APIs on API 30+ and only falls back to
 * [WindowInsetsCompat] on older releases. The compat path must be avoided on API 34:
 * [WindowInsetsCompat.toWindowInsetsCompat] with an attached view runs
 * `Impl20.initTypeBoundingRectsMaps`, which iterates every inset type including
 * `SYSTEM_OVERLAYS` and calls `WindowInsets.Type.systemOverlays()` via
 * `TypeImpl34.toPlatformType`. On devices whose framework lacks that method this throws
 * `NoSuchMethodError` during insets dispatch, before our listener even runs
 * (see Crashlytics: 87073ca8fd3c019e5e5bb6172c9d96e1, Pixel 8 Pro on Android 14).
 * Requesting only statusBars/navigationBars through the platform API never touches
 * `systemOverlays()`, so it is safe on those devices.
 */
object InsetsUtils {

    fun padTopForStatusBar(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.setOnApplyWindowInsetsListener { v, insets ->
                v.updatePadding(
                    top = insets.getInsets(WindowInsets.Type.statusBars()).top
                )
                insets
            }
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
                insets
            }
        }
        ViewCompat.requestApplyInsets(view)
    }

    fun padBottomForNavigationBar(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.setOnApplyWindowInsetsListener { v, insets ->
                v.updatePadding(
                    bottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom
                )
                insets
            }
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
                insets
            }
        }
        ViewCompat.requestApplyInsets(view)
    }

    fun padBottomMarginForNavigationBar(view: View) {
        val baseMargin = (view.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.setOnApplyWindowInsetsListener { v, insets ->
                (v.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                    lp.bottomMargin = baseMargin +
                            insets.getInsets(WindowInsets.Type.navigationBars()).bottom
                    v.layoutParams = lp
                }
                insets
            }
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                (v.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                    lp.bottomMargin =
                        baseMargin + insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                    v.layoutParams = lp
                }
                insets
            }
        }
        ViewCompat.requestApplyInsets(view)
    }
}
