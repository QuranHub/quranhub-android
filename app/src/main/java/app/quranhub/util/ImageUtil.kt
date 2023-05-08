package app.quranhub.util;

import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;

public class ImageUtil {

    private ImageUtil() {/* prevent instantiation */}

    /**
     * Color matrix that flips the components (<code>-1.0f * c + 255 = 255 - c</code>)
     * and keeps the alpha intact.
     */
    private static final float[] NEGATIVE = {
            -1.0f, 0, 0, 0, 255, // red
            0, -1.0f, 0, 0, 255, // green
            0, 0, -1.0f, 0, 255, // blue
            0, 0, 0, 1.0f, 0  // alpha
    };


    /**
     * Invert the colors for the given drawable.
     *
     * @param drawable
     */
    public static void invertDrawable(Drawable drawable) {
        drawable.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
    }

}
