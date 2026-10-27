package app.quranhub.core.common.util

import android.os.BadParcelableException
import android.util.Log

/**
 * Works around a platform bug on some Android 15 builds (seen on Samsung) where the system
 * `ConnectivityThread` fails to unmarshal `NetworkCapabilities` because the framework class
 * `android.net.UidRange` is missing from the app class loader (mismatch between the
 * Connectivity mainline module and the OEM framework).
 *
 * The exception is thrown inside `ConnectivityManager$CallbackHandler` while delivering a
 * network callback registered by a library (e.g. Firebase), so there is no app frame to put a
 * try/catch around. We only swallow this exact failure on that exact thread; everything else
 * is forwarded to the previous handler (Crashlytics, then the system default).
 */
object ConnectivityCrashGuard {

    private const val TAG = "ConnectivityCrashGuard"
    private const val CONNECTIVITY_THREAD = "ConnectivityThread"
    private const val MISSING_CLASS = "android.net.UidRange"

    fun install() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (isUidRangeUnmarshalCrash(thread, throwable)) {
                // The ConnectivityThread dies, but the app keeps running; affected callbacks
                // only lose connectivity updates, which the libraries tolerate.
                Log.w(TAG, "Ignoring platform UidRange unmarshalling crash", throwable)
            } else {
                previous?.uncaughtException(thread, throwable)
            }
        }
    }

    internal fun isUidRangeUnmarshalCrash(thread: Thread, throwable: Throwable): Boolean {
        if (thread.name != CONNECTIVITY_THREAD) return false
        if (throwable !is BadParcelableException) return false
        var cause: Throwable? = throwable
        while (cause != null) {
            if (cause is ClassNotFoundException && cause.message?.contains(MISSING_CLASS) == true) {
                return true
            }
            cause = cause.cause
        }
        return throwable.message?.contains(MISSING_CLASS) == true
    }
}
