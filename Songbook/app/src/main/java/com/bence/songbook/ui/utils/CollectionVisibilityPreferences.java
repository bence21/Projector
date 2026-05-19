package com.bence.songbook.ui.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

public final class CollectionVisibilityPreferences {

    private static final String KEY_PREFIX = "collection_visibility_language_";

    private CollectionVisibilityPreferences() {
    }

    public static boolean hasSelection(Context context, long languageId) {
        return getPreferences(context).contains(getKey(languageId));
    }

    public static Set<Long> getSelectedCollectionIds(Context context, long languageId) {
        SharedPreferences sharedPreferences = getPreferences(context);
        Set<String> stored = sharedPreferences.getStringSet(getKey(languageId), null);
        Set<Long> selectedCollectionIds = new HashSet<>();
        if (stored == null) {
            return selectedCollectionIds;
        }
        for (String value : stored) {
            try {
                selectedCollectionIds.add(Long.parseLong(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return selectedCollectionIds;
    }

    public static void saveSelectedCollectionIds(Context context, long languageId, Set<Long> collectionIds) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        if (collectionIds == null || collectionIds.isEmpty()) {
            editor.remove(getKey(languageId));
        } else {
            Set<String> stored = new HashSet<>(collectionIds.size());
            for (Long collectionId : collectionIds) {
                if (collectionId != null) {
                    stored.add(String.valueOf(collectionId));
                }
            }
            if (stored.isEmpty()) {
                editor.remove(getKey(languageId));
            } else {
                editor.putStringSet(getKey(languageId), stored);
            }
        }
        editor.apply();
    }

    public static void clearSelectedCollectionIds(Context context, long languageId) {
        getPreferences(context).edit().remove(getKey(languageId)).apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static String getKey(long languageId) {
        return KEY_PREFIX + languageId;
    }
}
