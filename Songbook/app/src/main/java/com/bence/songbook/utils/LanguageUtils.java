package com.bence.songbook.utils;

import com.bence.songbook.models.Language;

import java.util.ArrayList;
import java.util.List;

public class LanguageUtils {

    /**
     * Finds new languages from online languages that don't exist in the existing languages list.
     * Optionally updates the size of existing languages if they match.
     *
     * @param existingLanguages The list of languages that already exist
     * @param onlineLanguages   The list of languages from the online source
     * @param updateSize        If true, updates the size of existing languages when a match is found
     * @return A list of new languages that don't exist in the existing languages list
     */
    public static List<Language> findNewLanguages(List<Language> existingLanguages,
                                                  List<Language> onlineLanguages,
                                                  boolean updateSize) {
        List<Language> newLanguages = new ArrayList<>();
        for (Language onlineLanguage : onlineLanguages) {
            boolean was = false;
            String onlineLanguageUuid = onlineLanguage.getUuid();
            for (Language language : existingLanguages) {
                if (onlineLanguageUuid.equals(language.getUuid())) {
                    if (updateSize) {
                        language.setSize(onlineLanguage.getSize());
                    }
                    was = true;
                    break;
                }
            }
            if (!was) {
                newLanguages.add(onlineLanguage);
            }
        }
        return newLanguages;
    }
}

