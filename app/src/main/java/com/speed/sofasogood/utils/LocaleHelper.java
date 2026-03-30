package com.speed.sofasogood.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS = "settings";
    private static final String KEY_LANG = "language";

    public static Context applyLocale(Context context) {
        String lang = getLanguage(context);
        Locale locale;
        if ("zh-TW".equals(lang)) {
            locale = Locale.TRADITIONAL_CHINESE;
        } else if ("ja".equals(lang)) {
            locale = Locale.JAPANESE;
        } else {
            locale = Locale.ENGLISH;
        }
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }

    public static String getLanguage(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_LANG, "en");
    }

    public static void setLanguage(Context context, String lang) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_LANG, lang).apply();
    }
}
