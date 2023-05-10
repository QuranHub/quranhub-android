package app.quranhub.data.local.prefs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.quranhub.data.Constants;
import app.quranhub.util.LocaleUtils;
import app.quranhub.util.SharedPrefsUtils;


public final class AppPreferencesManager {

    private AppPreferencesManager() { /* prevent instantiation */}

    private static final String PREF_NIGHT_MODE_SETTING = "PREF_NIGHT_MODE_SETTING";
    private static final String PREF_APP_LANG_SETTING = "PREF_APP_LANG_SETTING";
    private static final String PREF_SCREEN_READING_BACKLIGHT_SETTING = "PREF_SCREEN_READING_BACKLIGHT_SETTING";
    private static final String PREF_LAST_READ_PAGE_SETTING = "PREF_LAST_READ_PAGE";
    private static final String PREF_RECITATION_SETTING = "PREF_RECITATION_SETTING";
    private static final String PREF_FIRST_TIME_WIZARD_SHOW_FLAG = "PREF_FIRST_TIME_WIZARD_SHOW_FLAG";
    private static final String PREF_QURAN_TRANSLATION_LANGUAGE = "PREF_QURAN_TRANSLATION_LANGUAGE";
    private static final String PREF_QURAN_TRANSLATION_BOOK = "PREF_QURAN_TRANSLATION_BOOK";
    private static final String PREF_RECITER_SHEIKH_SETTING = "PREF_RECITER_SHEIKH_SETTING";
    private static final String PREF_IS_DB_INITIALIZED = "PREF_IS_DB_INITIALIZED";

    public static boolean getNightModeSetting(@NonNull Context context) {
        return SharedPrefsUtils.getBoolean(context, PREF_NIGHT_MODE_SETTING, false);
    }

    public static void persistNightModeSetting(@NonNull Context context, boolean nightMode) {
        SharedPrefsUtils.saveBoolean(context, PREF_NIGHT_MODE_SETTING, nightMode);
    }

    @NonNull
    public static String getAppLangSetting(@NonNull Context context) {
        String defaultLangCode;
        if (Constants.Language.CODES.contains(LocaleUtils.getAppLanguage())) {
            // System-defined app language is supported
            defaultLangCode = LocaleUtils.getAppLanguage();
        } else {
            defaultLangCode = Constants.Language.DEFAULT_APP_LANGUAGE;
        }

        return SharedPrefsUtils.getString(context, PREF_APP_LANG_SETTING
                , defaultLangCode);
    }

    public static void persistAppLangSetting(@NonNull Context context, @NonNull String langCode) {
        SharedPrefsUtils.saveString(context, PREF_APP_LANG_SETTING, langCode);
        SharedPrefsUtils.clearPreference(context, PREF_QURAN_TRANSLATION_BOOK);
    }

    public static boolean getScreenReadingBacklightSetting(@NonNull Context context) {
        return SharedPrefsUtils.getBoolean(context, PREF_SCREEN_READING_BACKLIGHT_SETTING, true);
    }

    public static void persistScreenReadingBacklightSetting(@NonNull Context context, boolean enable) {
        SharedPrefsUtils.saveBoolean(context, PREF_SCREEN_READING_BACKLIGHT_SETTING, enable);
    }

    public static boolean getLastReadPageSetting(@NonNull Context context) {
        return SharedPrefsUtils.getBoolean(context, PREF_LAST_READ_PAGE_SETTING, true);
    }

    public static void persistLastReadPageSetting(@NonNull Context context, boolean enable) {
        SharedPrefsUtils.saveBoolean(context, PREF_LAST_READ_PAGE_SETTING, enable);
    }

    public static int getRecitationSetting(@NonNull Context context) {
        return SharedPrefsUtils.getInteger(context, PREF_RECITATION_SETTING, Constants.Recitation.HAFS_ID);
    }

    /**
     * Changing the recitation will also reset the current reciter sheikh setting.
     *
     * @param context
     * @param recitationId
     * @return Whether the recitation setting has changed (and the reciter was reset) or not.
     */
    public static boolean persistRecitationSetting(@NonNull Context context, int recitationId) {
        if (recitationId != getRecitationSetting(context)) {
            resetReciterSheikhSetting(context);
            SharedPrefsUtils.saveInteger(context, PREF_RECITATION_SETTING, recitationId);
            return true;
        }
        return false;
    }

    @Nullable
    public static String getReciterSheikhSetting(@NonNull Context context) {
        return SharedPrefsUtils.getString(context, PREF_RECITER_SHEIKH_SETTING, null);
    }

    public static void persistReciterSheikhSetting(@NonNull Context context, String reciterSheikhId) {
        SharedPrefsUtils.saveString(context, PREF_RECITER_SHEIKH_SETTING, reciterSheikhId);
    }

    public static void resetReciterSheikhSetting(@NonNull Context context) {
        SharedPrefsUtils.clearPreference(context, PREF_RECITER_SHEIKH_SETTING);
    }

    public static boolean isFirstTimeWizardDone(@NonNull Context context) {
        return SharedPrefsUtils.getBoolean(context, PREF_FIRST_TIME_WIZARD_SHOW_FLAG, false);
    }

    public static void markFirstTimeWizardDone(@NonNull Context context) {
        SharedPrefsUtils.saveBoolean(context, PREF_FIRST_TIME_WIZARD_SHOW_FLAG, true);
    }

    @NonNull
    public static String getQuranTranslationLanguage(@NonNull Context context) {
        return SharedPrefsUtils.getString(context, PREF_QURAN_TRANSLATION_LANGUAGE
                , getAppLangSetting(context));
    }

    public static void persistQuranTranslationLanguage(@NonNull Context context, @NonNull String langCode) {
        SharedPrefsUtils.saveString(context, PREF_QURAN_TRANSLATION_LANGUAGE, langCode);
        SharedPrefsUtils.clearPreference(context, PREF_QURAN_TRANSLATION_BOOK);
    }

    @Nullable
    public static String getQuranTranslationBook(@NonNull Context context) {
        return SharedPrefsUtils.getString(context, PREF_QURAN_TRANSLATION_BOOK, null);
    }

    public static void persistQuranTranslationBook(@NonNull Context context, @NonNull String translationBookId) {
        SharedPrefsUtils.saveString(context, PREF_QURAN_TRANSLATION_BOOK, translationBookId);
    }

    @Nullable
    public static String getQuranBookDbName(@NonNull Context context) {
        return SharedPrefsUtils.getString(context, "book_db_name", null);
    }

    public static void persistBookDbName(@NonNull Context context, @NonNull String dbName) {
        SharedPrefsUtils.saveString(context, "book_db_name", dbName);
    }

    @Nullable
    public static String getQuranBookName(@NonNull Context context) {
        return SharedPrefsUtils.getString(context, "book_db_name", null);
    }

    public static void persistBookName(@NonNull Context context, @NonNull String dbName) {
        SharedPrefsUtils.saveString(context, "book_db_name", dbName);
    }

    public static boolean isDbInitialized(@NonNull Context context) {
        return SharedPrefsUtils.getBoolean(context, PREF_IS_DB_INITIALIZED, false);
    }

    public static void persistDbInitialized(@NonNull Context context, boolean isInitialized) {
        SharedPrefsUtils.saveBoolean(context, PREF_IS_DB_INITIALIZED, isInitialized);
    }
}
