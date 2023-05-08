package app.quranhub.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;

import java.util.Locale;

import app.quranhub.data.local.prefs.AppPreferencesManager;

public final class LocaleUtils {

    private static final String TAG = LocaleUtils.class.getSimpleName();

    private LocaleUtils() { /* prevent instantiation */}

    public static String getAppLanguage() {
        return Locale.getDefault().getLanguage();
    }

    @SuppressLint("ObsoleteSdkInt")
    @NonNull
    public static Context setAppLanguage(@NonNull Context context, @NonNull String langCode) {
        Log.d(TAG, "Setting app language: " + langCode);

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }

        return context;
    }

    @NonNull
    public static Context initAppLanguage(@NonNull Context context) {
        return setAppLanguage(context, AppPreferencesManager.getAppLangSetting(context));
    }

    public static String formatNumber(@NonNull String num) {
        if (LocaleUtils.getAppLanguage().equals("ar")) {
            StringBuilder arabicNumber = new StringBuilder();
            char[] numMapper = new char[]{
                    '٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'};

            for (int i = 0; i < num.length(); i++) {
                arabicNumber.append(numMapper[Integer.parseInt(String.valueOf(num.charAt(i)))]);
            }
            return arabicNumber.toString();
        } else {
            return num;
        }
    }

    public static String formatNumber(int num) {
        return formatNumber(Integer.toString(num));
    }

    public static boolean isRTL(Context context) {
        return context.getResources().getConfiguration().getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

}
