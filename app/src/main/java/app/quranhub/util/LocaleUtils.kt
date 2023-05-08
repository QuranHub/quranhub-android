package app.quranhub.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.core.view.ViewCompat
import app.quranhub.data.local.prefs.AppPreferencesManager
import java.util.Locale

object LocaleUtils {

    private val TAG = LocaleUtils::class.java.simpleName

    @JvmStatic
    val appLanguage: String
        get() = Locale.getDefault().language

    @JvmStatic
    @SuppressLint("ObsoleteSdkInt")
    fun setAppLanguage(ctx: Context, langCode: String): Context {
        var context = ctx
        Log.d(TAG, "Setting app language: $langCode")
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale)
            context = context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return context
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