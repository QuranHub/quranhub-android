package app.quranhub

import android.content.Context
import android.content.res.Configuration
import androidx.multidex.MultiDexApplication
import app.quranhub.util.LocaleUtils
import com.downloader.PRDownloader
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize

class QuranhubApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        LocaleUtils.initAppLanguage(this)

        // Initialize App Check with the Play Integrity provider.
        // Must run before any other Firebase SDK is used.
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // initialize PRDownloader library (for downloading files)
        PRDownloader.initialize(applicationContext)
    }

    // Called by the system when the device configuration changes while your component is running.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Android resets the locale for the top level resources back to the device default
        // on every application restart and configuration change.
        LocaleUtils.initAppLanguage(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleUtils.initAppLanguage(base))
    }

    companion object {
        private val TAG = QuranhubApplication::class.java.simpleName
    }
}