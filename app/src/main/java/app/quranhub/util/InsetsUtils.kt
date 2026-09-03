package app.quranhub.util

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

object InsetsUtils {

    fun padTopForStatusBar(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    fun padBottomForNavigationBar(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    fun padBottomMarginForNavigationBar(view: View) {
        val baseMargin = (view.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            (v.layoutParams as? ViewGroup.MarginLayoutParams)?.let { lp ->
                lp.bottomMargin =
                    baseMargin + insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                v.layoutParams = lp
            }
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }
}
