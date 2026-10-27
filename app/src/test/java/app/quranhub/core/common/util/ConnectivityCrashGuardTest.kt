package app.quranhub.core.common.util

import app.quranhub.core.common.util.ConnectivityCrashGuard.CALLBACK_HANDLER
import app.quranhub.core.common.util.ConnectivityCrashGuard.CONNECTIVITY_THREAD
import app.quranhub.core.common.util.ConnectivityCrashGuard.MISSING_CLASS
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectivityCrashGuardTest {

    private fun crash(
        missingClass: String = MISSING_CLASS,
        fromCallbackHandler: Boolean = true
    ): Throwable {
        val e = RuntimeException(
            "ClassNotFoundException when unmarshalling: $missingClass",
            ClassNotFoundException(missingClass)
        )
        val frames = mutableListOf(StackTraceElement("android.os.Parcel", "readParcelable", null, 1))
        if (fromCallbackHandler) {
            frames += StackTraceElement(CALLBACK_HANDLER, "handleMessage", null, 1)
        }
        e.stackTrace = frames.toTypedArray()
        return e
    }

    @Test
    fun matches_uidRangeCrashOnConnectivityThread() {
        assertTrue(ConnectivityCrashGuard.matches(CONNECTIVITY_THREAD, crash()))
    }

    @Test
    fun ignores_otherThreads() {
        assertFalse(ConnectivityCrashGuard.matches("main", crash()))
    }

    @Test
    fun ignores_otherMissingClasses() {
        assertFalse(ConnectivityCrashGuard.matches(CONNECTIVITY_THREAD, crash("android.net.Foo")))
        assertFalse(
            ConnectivityCrashGuard.matches(CONNECTIVITY_THREAD, crash("android.net.UidRangeX"))
        )
    }

    @Test
    fun ignores_throwsOutsideCallbackHandler() {
        assertFalse(
            ConnectivityCrashGuard.matches(CONNECTIVITY_THREAD, crash(fromCallbackHandler = false))
        )
    }

    @Test
    fun ignores_noCause() {
        val e = RuntimeException(MISSING_CLASS)
        e.stackTrace = arrayOf(StackTraceElement(CALLBACK_HANDLER, "handleMessage", null, 1))
        assertFalse(ConnectivityCrashGuard.matches(CONNECTIVITY_THREAD, e))
    }
}
