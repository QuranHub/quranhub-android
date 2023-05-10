package app.quranhub.data.local.prefs

import android.content.Context
import app.quranhub.data.Constants
import app.quranhub.util.LocaleUtils.appLanguage
import app.quranhub.util.SharedPrefsUtils.clearPreference
import app.quranhub.util.SharedPrefsUtils.getBoolean
import app.quranhub.util.SharedPrefsUtils.getFloat
import app.quranhub.util.SharedPrefsUtils.getInteger
import app.quranhub.util.SharedPrefsUtils.getString
import app.quranhub.util.SharedPrefsUtils.saveBoolean
import app.quranhub.util.SharedPrefsUtils.saveFloat
import app.quranhub.util.SharedPrefsUtils.saveInteger
import app.quranhub.util.SharedPrefsUtils.saveString

object AppPreferencesManager {

    private const val PREF_NIGHT_MODE_SETTING = "PREF_NIGHT_MODE_SETTING"
    private const val PREF_QURAN_PAGE_SCALE = "PREF_QURAN_PAGE_SCALE"
    private const val PREF_APP_LANG_SETTING = "PREF_APP_LANG_SETTING"
    private const val PREF_SCREEN_READING_BACKLIGHT_SETTING =
        "PREF_SCREEN_READING_BACKLIGHT_SETTING"
    private const val PREF_LAST_READ_PAGE_SETTING = "PREF_LAST_READ_PAGE"
    private const val PREF_RECITATION_SETTING = "PREF_RECITATION_SETTING"
    private const val PREF_FIRST_TIME_WIZARD_SHOW_FLAG = "PREF_FIRST_TIME_WIZARD_SHOW_FLAG"
    private const val PREF_QURAN_TRANSLATION_LANGUAGE = "PREF_QURAN_TRANSLATION_LANGUAGE"
    private const val PREF_QURAN_TRANSLATION_BOOK = "PREF_QURAN_TRANSLATION_BOOK"
    private const val PREF_RECITER_SHEIKH_SETTING = "PREF_RECITER_SHEIKH_SETTING"
    private const val PREF_IS_DB_INITIALIZED = "PREF_IS_DB_INITIALIZED"

    @JvmStatic
    fun getNightModeSetting(context: Context): Boolean {
        return getBoolean(context, PREF_NIGHT_MODE_SETTING, false)
    }

    @JvmStatic
    fun persistNightModeSetting(context: Context, nightMode: Boolean) {
        saveBoolean(context, PREF_NIGHT_MODE_SETTING, nightMode)
    }

    @JvmStatic
    fun getQuranPageZoomScaleSetting(context: Context): Float {
        return getFloat(context, PREF_QURAN_PAGE_SCALE, 1f)
    }

    @JvmStatic
    fun persistQuranPageZoomScaleSetting(context: Context, scale: Float) {
        if (scale < 1f) return
        saveFloat(context, PREF_QURAN_PAGE_SCALE, scale)
    }

    @JvmStatic
    fun getAppLangSetting(context: Context): String {
        val defaultLangCode: String = if (Constants.Language.CODES.contains(appLanguage)) {
            // System-defined app language is supported
            appLanguage
        } else {
            Constants.Language.DEFAULT_APP_LANGUAGE
        }
        return getString(
            context, PREF_APP_LANG_SETTING, defaultLangCode
        )!!
    }

    fun persistAppLangSetting(context: Context, langCode: String) {
        saveString(context, PREF_APP_LANG_SETTING, langCode)
        clearPreference(context, PREF_QURAN_TRANSLATION_BOOK)
    }

    fun getScreenReadingBacklightSetting(context: Context): Boolean {
        return getBoolean(context, PREF_SCREEN_READING_BACKLIGHT_SETTING, true)
    }

    fun persistScreenReadingBacklightSetting(context: Context, enable: Boolean) {
        saveBoolean(context, PREF_SCREEN_READING_BACKLIGHT_SETTING, enable)
    }

    fun getLastReadPageSetting(context: Context): Boolean {
        return getBoolean(context, PREF_LAST_READ_PAGE_SETTING, true)
    }

    fun persistLastReadPageSetting(context: Context, enable: Boolean) {
        saveBoolean(context, PREF_LAST_READ_PAGE_SETTING, enable)
    }

    @JvmStatic
    fun getRecitationSetting(context: Context): Int {
        return getInteger(context, PREF_RECITATION_SETTING, Constants.Recitation.HAFS_ID)
    }

    /**
     * Changing the recitation will also reset the current reciter sheikh setting.
     *
     * @param context
     * @param recitationId
     * @return Whether the recitation setting has changed (and the reciter was reset) or not.
     */
    fun persistRecitationSetting(context: Context, recitationId: Int): Boolean {
        if (recitationId != getRecitationSetting(context)) {
            resetReciterSheikhSetting(context)
            saveInteger(context, PREF_RECITATION_SETTING, recitationId)
            return true
        }
        return false
    }

    fun getReciterSheikhSetting(context: Context): String? {
        return getString(context, PREF_RECITER_SHEIKH_SETTING, null)
    }

    fun persistReciterSheikhSetting(context: Context, reciterSheikhId: String?) {
        saveString(context, PREF_RECITER_SHEIKH_SETTING, reciterSheikhId!!)
    }

    fun resetReciterSheikhSetting(context: Context) {
        clearPreference(context, PREF_RECITER_SHEIKH_SETTING)
    }

    fun isFirstTimeWizardDone(context: Context): Boolean {
        return getBoolean(context, PREF_FIRST_TIME_WIZARD_SHOW_FLAG, false)
    }

    fun markFirstTimeWizardDone(context: Context) {
        saveBoolean(context, PREF_FIRST_TIME_WIZARD_SHOW_FLAG, true)
    }

    @JvmStatic
    fun getQuranTranslationLanguage(context: Context): String {
        return getString(
            context, PREF_QURAN_TRANSLATION_LANGUAGE, getAppLangSetting(context)
        )!!
    }

    @JvmStatic
    fun persistQuranTranslationLanguage(context: Context, langCode: String) {
        saveString(context, PREF_QURAN_TRANSLATION_LANGUAGE, langCode)
        clearPreference(context, PREF_QURAN_TRANSLATION_BOOK)
    }

    @JvmStatic
    fun getQuranTranslationBook(context: Context): String? {
        return getString(context, PREF_QURAN_TRANSLATION_BOOK, null)
    }

    @JvmStatic
    fun persistQuranTranslationBook(context: Context, translationBookId: String) {
        saveString(context, PREF_QURAN_TRANSLATION_BOOK, translationBookId)
    }

    fun getQuranBookDbName(context: Context): String? {
        return getString(context, "book_db_name", null)
    }

    @JvmStatic
    fun persistBookDbName(context: Context, dbName: String) {
        saveString(context, "book_db_name", dbName)
    }

    fun getQuranBookName(context: Context): String? {
        return getString(context, "book_db_name", null)
    }

    @JvmStatic
    fun persistBookName(context: Context, dbName: String) {
        saveString(context, "book_db_name", dbName)
    }

    @JvmStatic
    fun isDbInitialized(context: Context): Boolean {
        return getBoolean(context, PREF_IS_DB_INITIALIZED, false)
    }

    @JvmStatic
    fun persistDbInitialized(context: Context, isInitialized: Boolean) {
        saveBoolean(context, PREF_IS_DB_INITIALIZED, isInitialized)
    }
}