package app.quranhub.util

import androidx.fragment.app.Fragment

object FragmentUtils {

    /**
     * Check if fragment is active and is safe to do actions with.
     *
     * @param fragment The fragment to check.
     * @return Whether the fragment is active & safe to do actions with or not.
     */
    @JvmStatic
    fun isSafeFragment(fragment: Fragment): Boolean {
        return !(fragment.isRemoving || fragment.activity == null || fragment.isDetached
                || !fragment.isAdded || fragment.view == null);
    }
}