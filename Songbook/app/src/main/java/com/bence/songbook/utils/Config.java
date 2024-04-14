package com.bence.songbook.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bence.songbook.api.YouTubeApiBean;

public class Config {

    private static Config instance;
    private final String YOUTUBE_API_KEY = "YouTubeApiKey";

    private Config() {
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public String getYouTubeApiKey(Context context) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String youTubeApiKey = sharedPreferences.getString(YOUTUBE_API_KEY, "");
            if (youTubeApiKey != null && !youTubeApiKey.isEmpty()) {
                return youTubeApiKey;
            }
            loadYouTubeApiKey(sharedPreferences);
        } catch (Exception ignored) {
        }
        return "x";
    }

    private void loadYouTubeApiKey(SharedPreferences sharedPreferences) {
        new Thread(() -> {
            YouTubeApiBean youTubeApiBean = new YouTubeApiBean();
            String s = youTubeApiBean.getYouTubeApiKey();
            sharedPreferences.edit().putString(YOUTUBE_API_KEY, s).apply();
        }).start();
    }
}
