package com.bence.songbook.api.downloader;

import com.bence.songbook.api.LanguageApiBean;
import com.bence.songbook.models.Language;
import com.bence.songbook.utils.LanguageUtils;

import java.util.ArrayList;
import java.util.List;

public class LanguageDownloader extends Thread {

    public LanguageDownloader(List<Language> languages, UpdateListener updateListener) {
        super(() -> {
            try {
                LanguageApiBean languageApi = new LanguageApiBean();
                List<Language> onlineLanguages = languageApi.getLanguages();
                if (onlineLanguages != null) {
                    List<Language> newLanguages = LanguageUtils.findNewLanguages(languages, onlineLanguages, false);
                    List<Language> withNewLanguages = new ArrayList<>(languages.size());
                    withNewLanguages.addAll(languages);
                    withNewLanguages.addAll(newLanguages);
                    updateListener.onUpdated(withNewLanguages);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public interface UpdateListener {
        void onUpdated(List<Language> languages);
    }
}