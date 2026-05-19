package com.example.weighttrainingtracker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class ThemeSetter extends Application {
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_THEME = "theme";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;

    // Ensure this method is public
    public static void applyTheme(Context context) {
        int theme = getThemePreference(context);
        if (theme == THEME_DARK) {
            context.setTheme(R.style.NightThemeWeightTrainingTracker); // Set dark theme
        } else {
            context.setTheme(R.style.LightThemeWeightTrainingTracker); // Set light theme
        }
    }

    public static void setThemePreference(Context context, int theme) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_THEME, theme);
        editor.apply();
    }

    public static int getThemePreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME, THEME_LIGHT); // Default to Light theme
    }
}
