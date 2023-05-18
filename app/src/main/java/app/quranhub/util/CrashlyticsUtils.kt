package app.quranhub.util

import android.content.Context
import android.os.Build
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase

fun addCrashlyticsCustomKeys(context: Context) {
    val appInstaller = getAppInstaller(context)
    Firebase.crashlytics.setCustomKeys {
        key("App Installer", appInstaller)
    }
}

/**
 * Retrieves the name of the app responsible for the installation of this app.
 * This can help in identifying which market this app was installed from or whether the user
 * sideloaded it using an APK (Package Installer).
 */
private fun getAppInstaller(context: Context): String {
    val appContext = context.applicationContext

    val installerPackageName = try {
        val appPackageManager = appContext.packageManager
        val appPackageName = appContext.packageName

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            appPackageManager.getInstallSourceInfo(appPackageName).installingPackageName
        else
            appPackageManager.getInstallerPackageName(appPackageName)
    } catch (e: Exception) {
        e.printStackTrace()
        "--"
    }

    return when (installerPackageName) {
        "com.android.vending" -> "Google Play Store"
        "com.amazon.venezia" -> "Amazon AppStore"
        "com.huawei.appmarket" -> "Huawei AppGallery"
        "com.google.android.packageinstaller" -> "Package Installer"
        else -> installerPackageName ?: "Unknown"
    }
}