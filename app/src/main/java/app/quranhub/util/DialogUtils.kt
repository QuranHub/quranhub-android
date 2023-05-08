package app.quranhub.util

import android.view.WindowManager
import androidx.fragment.app.DialogFragment

object DialogUtils {

    // Declare dialogs width & height to be proportional to screen size
    // for example width 0.8 means 80% of total screen width
    const val DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT = 0.8f
    const val DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE = 0.5f
    const val DIALOG_STD_HEIGHT_SCREEN_RATIO_PORTRAIT = 0.6f
    const val DIALOG_STD_HEIGHT_SCREEN_RATIO_LANDSCAPE = 0.9f

    /**
     * Call this method from DialogFragment#onResume callback to adjust the dialog size correctly.
     * Adjusts dialog width & height to be proportional (ratio) to screen size.
     * For example, widthScreenRatioPortrait 0.8 means 80% of total screen width in portrait mode, etc...
     *
     * @param dialogFragment
     * @param widthScreenRatioPortrait
     * @param heightScreenRatioPortrait
     * @param widthScreenRatioLandscape
     * @param heightScreenRatioLandscape
     */
    /**
     * Call this method from DialogFragment#onResume callback to adjust the dialog size correctly.
     *
     * @param dialogFragment
     */
    @JvmStatic
    @JvmOverloads
    fun adjustDialogSize(
        dialogFragment: DialogFragment,
        widthScreenRatioPortrait: Float = DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT,
        heightScreenRatioPortrait: Float = DIALOG_STD_HEIGHT_SCREEN_RATIO_PORTRAIT,
        widthScreenRatioLandscape: Float = DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE,
        heightScreenRatioLandscape: Float = DIALOG_STD_HEIGHT_SCREEN_RATIO_LANDSCAPE
    ) {
        val totalWidth = dialogFragment.resources.displayMetrics.widthPixels
        val totalHeight = dialogFragment.resources.displayMetrics.heightPixels
        if (ScreenUtils.isPortrait(dialogFragment.requireContext())) {
            dialogFragment.dialog!!.window!!
                .setLayout(
                    (totalWidth * widthScreenRatioPortrait).toInt(),
                    (totalHeight * heightScreenRatioPortrait).toInt()
                )
        } else {
            dialogFragment.dialog!!.window!!
                .setLayout(
                    (totalWidth * widthScreenRatioLandscape).toInt(),
                    (totalHeight * heightScreenRatioLandscape).toInt()
                )
        }
    }

    @JvmStatic
    fun wrapDialogHeight(dialogFragment: DialogFragment) {
        val totalWidth = dialogFragment.resources.displayMetrics.widthPixels
        if (ScreenUtils.isPortrait(dialogFragment.requireContext())) {
            dialogFragment.dialog!!.window!!.setLayout(
                (totalWidth * DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        } else {
            dialogFragment.dialog!!.window!!.setLayout(
                (totalWidth * DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun adjustLandscapeDialogSize(dialogFragment: DialogFragment) {
        val totalWidth = dialogFragment.resources.displayMetrics.widthPixels
        val totalHeight = dialogFragment.resources.displayMetrics.heightPixels
        if (ScreenUtils.isPortrait(dialogFragment.requireContext())) {
            dialogFragment.dialog!!.window!!.setLayout(
                (totalWidth * DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT).toInt(),
                totalHeight
            )
        } else {
            dialogFragment.dialog!!.window!!.setLayout(
                (totalWidth * DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE).toInt(),
                (totalHeight * DIALOG_STD_HEIGHT_SCREEN_RATIO_LANDSCAPE).toInt()
            )
        }
    }
}