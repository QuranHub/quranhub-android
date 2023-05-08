package app.quranhub.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

public final class SharedPrefsUtils {

    private static final String TAG = SharedPrefsUtils.class.getSimpleName();

    private SharedPrefsUtils() { /* prevent instantiation */ }

    private static final String PREF_FILE_NAME = "mushaf_prefs";

    public static void saveString(@NonNull Context context, @NonNull String key, @NonNull String value) {
        if (context != null) {
            getSharedPreference(context).edit().putString(key, value).apply();
        } else {
            Log.e(TAG, "Couldn't save string; Passed context is null.");
        }
    }

    public static void saveDouble(@NonNull Context context, @NonNull String key, float value) {
        if (context != null) {
            getSharedPreference(context).edit().putFloat(key, value).apply();
        } else {
            Log.e(TAG, "Couldn't save double; Passed context is null.");
        }
    }

    public static void saveInteger(@NonNull Context context, @NonNull String key, int value) {
        if (context != null) {
            getSharedPreference(context).edit().putInt(key, value).apply();
        } else {
            Log.e(TAG, "Couldn't save integer; Passed context is null.");
        }
    }

    public static void saveBoolean(@NonNull Context context, @NonNull String key, boolean value) {
        if (context != null) {
            getSharedPreference(context).edit().putBoolean(key, value).apply();
        } else {
            Log.e(TAG, "Couldn't save boolean; Passed context is null.");
        }
    }

    public static int getInteger(@NonNull Context context, @NonNull String key, int defValue) {
        if (context != null) {
            return getSharedPreference(context).getInt(key, defValue);
        } else {
            Log.e(TAG, "Couldn't get integer; Passed context is null. Returning default value.");
            return defValue;
        }
    }

    public static boolean getBoolean(@NonNull Context context, @NonNull String key, boolean defValue) {
        if (context != null) {
            return getSharedPreference(context).getBoolean(key, defValue);
        } else {
            Log.e(TAG, "Couldn't get boolean; Passed context is null. Returning default value.");
            return defValue;
        }
    }

    public static float getFloat(@NonNull Context context, @NonNull String key, float defValue) {
        if (context != null) {
            return getSharedPreference(context).getFloat(key, defValue);
        } else {
            Log.e(TAG, "Couldn't get float; Passed context is null. Returning default value.");
            return defValue;
        }
    }

    public static String getString(@NonNull Context context, @NonNull String key, String defValue) {
        if (context != null) {
            return getSharedPreference(context).getString(key, defValue);
        } else {
            Log.e(TAG, "Couldn't get string; Passed context is null. Returning default value.");
            return defValue;
        }
    }

    public static void clearPreference(@NonNull Context context, @NonNull String key) {
        if (context != null) {
            getSharedPreference(context).edit().remove(key).apply();
        } else {
            Log.e(TAG, "Couldn't clear preference; Passed context is null.");
        }
    }

    public static void clearAll(@NonNull Context context) {
        if (context != null) {
            getSharedPreference(context).edit().clear().apply();
        } else {
            Log.e(TAG, "Couldn't clear all saved values; Passed context is null.");
        }
    }

    private static SharedPreferences getSharedPreference(@NonNull Context context) {
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }
}
