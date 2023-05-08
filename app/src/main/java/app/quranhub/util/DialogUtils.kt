package app.quranhub.util;

import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public final class DialogUtils {

    private DialogUtils() { /* Prevent instantiation */ }

    // Declare dialogs width & height to be proportional to screen size
    // for example width 0.8 means 80% of total screen width
    public final static float DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT = 0.8f;
    public final static float DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE = 0.5f;
    public final static float DIALOG_STD_HEIGHT_SCREEN_RATIO_PORTRAIT = 0.6f;
    public final static float DIALOG_STD_HEIGHT_SCREEN_RATIO_LANDSCAPE = 0.9f;


    /**
     * Call this method from DialogFragment#onResume callback to adjust the dialog size correctly.
     *
     * @param dialogFragment
     */
    public static void adjustDialogSize(@NonNull DialogFragment dialogFragment) {
        adjustDialogSize(dialogFragment, DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT, DIALOG_STD_HEIGHT_SCREEN_RATIO_PORTRAIT
                , DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE, DIALOG_STD_HEIGHT_SCREEN_RATIO_LANDSCAPE);
    }

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
    public static void adjustDialogSize(@NonNull DialogFragment dialogFragment, float widthScreenRatioPortrait
            , float heightScreenRatioPortrait, float widthScreenRatioLandscape, float heightScreenRatioLandscape) {

        int totalWidth = dialogFragment.getResources().getDisplayMetrics().widthPixels;
        int totalHeight = dialogFragment.getResources().getDisplayMetrics().heightPixels;
        if (ScreenUtils.isPortrait(dialogFragment.getContext())) {
            dialogFragment.getDialog().getWindow().setLayout((int) (totalWidth * widthScreenRatioPortrait)
                    , (int) (totalHeight * heightScreenRatioPortrait));
        } else {
            dialogFragment.getDialog().getWindow().setLayout((int) (totalWidth * widthScreenRatioLandscape)
                    , (int) (totalHeight * heightScreenRatioLandscape));
        }

    }

    public static void wrapDialogHeight(@NonNull DialogFragment dialogFragment) {

        int totalWidth = dialogFragment.getResources().getDisplayMetrics().widthPixels;
        if (ScreenUtils.isPortrait(dialogFragment.getContext())) {
            dialogFragment.getDialog().getWindow().setLayout((int) (totalWidth * DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT)
                    , WindowManager.LayoutParams.WRAP_CONTENT);
        } else {
            dialogFragment.getDialog().getWindow().setLayout((int) (totalWidth * DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE)
                    , WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }


    public static void adjustLandscapeDialogSize(DialogFragment dialogFragment) {
        int totalWidth = dialogFragment.getResources().getDisplayMetrics().widthPixels;
        int totalHeight = dialogFragment.getResources().getDisplayMetrics().heightPixels;
        if (ScreenUtils.isPortrait(dialogFragment.getContext())) {
            dialogFragment.getDialog().getWindow().setLayout((int) (totalWidth * DIALOG_STD_WIDTH_SCREEN_RATIO_PORTRAIT), totalHeight);
        } else {
            dialogFragment.getDialog().getWindow().setLayout((int) (totalWidth * DIALOG_STD_WIDTH_SCREEN_RATIO_LANDSCAPE)
                    , (int) (totalHeight * DIALOG_STD_HEIGHT_SCREEN_RATIO_LANDSCAPE));
        }
    }
}
