package app.quranhub.util

import android.content.Context
import android.content.SharedPreferences

object SharedPrefsUtils {

    private val TAG = SharedPrefsUtils::class.java.simpleName

    private const val PREF_FILE_NAME = "mushaf_prefs"

    @JvmStatic
    fun saveString(context: Context, key: String, value: String) {
        getSharedPreference(context).edit().putString(key, value).apply()
    }

    fun saveFloat(context: Context, key: String, value: Float) {
        getSharedPreference(context).edit().putFloat(key, value).apply()
    }

    fun getFloat(context: Context, key: String, defValue: Float): Float {
        return getSharedPreference(context).getFloat(key, defValue)
    }

    @JvmStatic
    fun saveInteger(context: Context, key: String, value: Int) {
        getSharedPreference(context).edit().putInt(key, value).apply()
    }

    @JvmStatic
    fun saveBoolean(context: Context, key: String, value: Boolean) {
        getSharedPreference(context).edit().putBoolean(key, value).apply()
    }

    @JvmStatic
    fun getInteger(context: Context, key: String, defValue: Int): Int {
        return getSharedPreference(context).getInt(key, defValue)
    }

    @JvmStatic
    fun getBoolean(context: Context, key: String, defValue: Boolean): Boolean {
        return getSharedPreference(context).getBoolean(key, defValue)
    }

    @JvmStatic
    fun getString(context: Context, key: String, defValue: String?): String? {
        return getSharedPreference(context).getString(key, defValue)
    }

    @JvmStatic
    fun clearPreference(context: Context, key: String) {
        getSharedPreference(context).edit().remove(key).apply()
    }

    fun clearAll(context: Context) {
        getSharedPreference(context).edit().clear().apply()
    }

    private fun getSharedPreference(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
    }
}