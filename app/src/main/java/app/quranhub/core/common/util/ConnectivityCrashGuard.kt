package app.quranhub.core.common.util

import android.os.BadParcelableException
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Works around a platform bug on some Android 15 builds (seen on Samsung) where the system
 * `ConnectivityThread` fails to unmarshal `NetworkCapabilities` because the framework class
 * `android.net.UidRange` is missing from the app class loader (mismatch between the
 * Connectivity mainline module and the OEM framework).
 *
 * The exception is thrown inside `ConnectivityManager$CallbackHandler` while delivering a
 * network callback registered by a library (e.g. Firebase), so there is no app frame to put a
 * try/catch around. Only this exact failure is swallowed; everything else is forwarded to the
 * previous handler (Crashlytics, then the system default).
 *
 * Caveat: the uncaught exception still terminates that HandlerThread, so connectivity
 * callbacks stop being delivered for the rest of the process. That is strictly better than
 * killing the app, and the event is still reported to Crashlytics as a non-fatal.
 */
object ConnectivityCrashGuard {

    private const val TAG = "ConnectivityCrashGuard"
    internal const val CONNECTIVITY_THREAD = "ConnectivityThread"
    internal const val MISSING_CLASS = "android.net.UidRange"
    internal const val CALLBACK_HANDLER = "android.net.ConnectivityManager\$CallbackHandler"

    fun install() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (throwable is BadParcelableException && matches(thread.name, throwable)) {
                Log.w(TAG, "Ignoring platform UidRange unmarshalling crash", throwable)
                runCatching { FirebaseCrashlytics.getInstance().recordException(throwable) }
            } else {
                previous?.uncaughtException(thread, throwable)
            }
        }
    }

    /**
     * Type-independent part of the match, so it can be unit tested without android.jar stubs:
     * the right thread, a [ClassNotFoundException] for exactly [MISSING_CLASS] in the cause
     * chain, and the throw coming from ConnectivityManager's callback handler.
     */
    internal fun matches(threadName: String, throwable: Throwable): Boolean {
        if (threadName != CONNECTIVITY_THREAD) return false
        if (throwable.stackTrace.none { it.className == CALLBACK_HANDLER }) return false
        return generateSequence(throwable) { it.cause }
            .take(10) // guard against cyclic cause chains
            .any { it is ClassNotFoundException && it.message == MISSING_CLASS }
    }
}
