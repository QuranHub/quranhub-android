package app.quranhub.core.common.util

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.core.view.ViewCompat
import app.quranhub.core.data.local.prefs.AppPreferencesManager
import java.util.Locale

object LocaleUtils {

    private val TAG = LocaleUtils::class.java.simpleName

    @JvmStatic
    val appLanguage: String
        get() = Locale.getDefault().language

    /**
     * Applies [langCode] to [ctx] and returns a configuration-wrapped context.
     * minSdk is 24, so createConfigurationContext is always available; the old
     * updateConfiguration / Configuration.locale path is unused and removed.
     */
    @JvmStatic
    fun setAppLanguage(ctx: Context, langCode: String): Context {
        Log.d(TAG, "Setting app language: $langCode")
        val locale = Locale.Builder().setLanguage(langCode).build()
        Locale.setDefault(locale)
        val config = Configuration(ctx.resources.configuration)
        config.setLocale(locale)
        return ctx.createConfigurationContext(config)
    }

    @JvmStatic
    fun initAppLanguage(context: Context): Context {
        return setAppLanguage(context, AppPreferencesManager.getAppLangSetting(context))
    }

    fun formatNumber(num: String): String {
        return if (appLanguage == "ar") {
            val arabicNumber = StringBuilder()
            val numMapper = charArrayOf(
                '٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'
            )
            for (element in num) {
                arabicNumber.append(numMapper[element.toString().toInt()])
            }
            arabicNumber.toString()
        } else {
            num
        }
    }

    @JvmStatic
    fun formatNumber(num: Int): String {
        return formatNumber(num.toString())
    }

    @JvmStatic
    fun isRTL(context: Context): Boolean {
        return context.resources.configuration.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
    }
}
