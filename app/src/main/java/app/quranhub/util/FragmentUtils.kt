package app.quranhub.util;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public final class FragmentUtils {

    private FragmentUtils() { /* prevent instantiation */}

    /**
     * Check if fragment is active and is safe to do actions with.
     *
     * @param fragment The fragment to check.
     * @return Whether the fragment is active & safe to do actions with or not.
     */
    public static boolean isSafeFragment(@NonNull Fragment fragment) {
        return !(fragment.isRemoving() || fragment.getActivity() == null || fragment.isDetached()
                || !fragment.isAdded() || fragment.getView() == null);
    }

}
