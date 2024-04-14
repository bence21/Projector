package com.bence.songbook.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bence.songbook.R;

public class Preferences {
    public static int getTheme(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean light_theme_switch = sharedPreferences.getBoolean("light_theme_switch", false);
        return light_theme_switch ? R.style.SongBookTheme_Light : R.style.SongBookTheme;
    }
}
