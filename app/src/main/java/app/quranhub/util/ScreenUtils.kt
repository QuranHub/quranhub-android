package app.quranhub.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import app.quranhub.ui.mushaf.model.ScreenSize;

public final class ScreenUtils {

    private static final String TAG = ScreenUtils.class.getSimpleName();

    public static final String PORTRAIT_STATE = "PORTRAIT";
    public static final String LANDSCAPE_STATE = "LANDSCAPE";

    private ScreenUtils() {
        // Prevent instantiation
    }

    public static String getOrientationState(@NonNull Context context) {

        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return PORTRAIT_STATE;
        } else {
            return LANDSCAPE_STATE;
        }
    }

    public static boolean isPortrait(@NonNull Context context) {
        return getOrientationState(context).equals(PORTRAIT_STATE);
    }

    public static boolean isLandscape(@NonNull Context context) {
        return getOrientationState(context).equals(LANDSCAPE_STATE);
    }

    public static int getStatusBarHeight(@NonNull Context context, @NonNull ImageView quranPageIv) {
        int[] coordOffset = new int[2];
        quranPageIv.getLocationOnScreen(coordOffset);
        int statusBarHeight = (int) Math.ceil(25 * context.getResources().getDisplayMetrics().density);
        return coordOffset[1] - statusBarHeight;
    }

    public static void dismissKeyboard(@NonNull Context context, @NonNull View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Gets the available screen width & height. Ignores the bottom system navigation bar (if exists).
     * Example: In PORTRAIT width:720, height:1184. In LANDSCAPE width:1184, height: 720.
     *
     * @param activity current activity reference.
     * @return ScreenSize instance holding width & height information.
     */
    public static ScreenSize getScreenSize(@NonNull Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        ScreenSize screenSize = new ScreenSize(size.x, size.y);
        Log.d(TAG, "getScreenSize(): width = " + screenSize.getWidth()
                + ", height = " + screenSize.getHeight());
        return screenSize;
    }

    /**
     * Control whether to keep the device's screen turned on and bright or to set it back to normal.
     *
     * @param activity current activity instance.
     * @param enable   whether to enable or disable this feature.
     */
    public static void keepScreenOn(@NonNull Activity activity, boolean enable) {
        if (enable) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public static boolean isLayoutRtl(@NonNull View view) {
        return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

}
