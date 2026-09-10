package com.bence.songbook.utils;

import com.bence.songbook.models.Language;
import com.bence.songbook.repository.LanguageRepository;
import com.bence.songbook.repository.SongRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Soft-deleted server languages must leave the download UI. Local rows with no songs are
     * removed from storage; rows that still have songs stay so local data is not orphaned.
     */
    public static void applySoftDeletedDownloadPolicy(List<Language> downloadLanguages,
                                                      List<Language> deletedFromServer,
                                                      LanguageRepository languageRepository,
                                                      SongRepository songRepository) {
        Set<String> softDeletedUuids = collectSoftDeletedUuids(deletedFromServer);
        syncLocalSoftDeletedLanguages(
                downloadLanguages, softDeletedUuids, languageRepository, songRepository);
        removeSoftDeletedFromDownloadList(downloadLanguages, softDeletedUuids);
    }

    static Set<String> collectSoftDeletedUuids(List<Language> deletedFromServer) {
        Set<String> softDeletedUuids = new HashSet<>();
        if (deletedFromServer == null) {
            return softDeletedUuids;
        }
        for (Language language : deletedFromServer) {
            String uuid = language.getUuid();
            if (uuid != null) {
                softDeletedUuids.add(uuid);
            }
        }
        return softDeletedUuids;
    }

    static void syncLocalSoftDeletedLanguages(List<Language> downloadLanguages,
                                              Set<String> softDeletedUuids,
                                              LanguageRepository languageRepository,
                                              SongRepository songRepository) {
        if (softDeletedUuids.isEmpty()) {
            return;
        }
        for (Language language : downloadLanguages) {
            String uuid = language.getUuid();
            if (uuid == null || !softDeletedUuids.contains(uuid)) {
                continue;
            }
            if (language.getSongsCount(songRepository) == 0) {
                languageRepository.delete(language);
                continue;
            }
            clearDownloadSelectionForSoftDeleted(language, languageRepository);
        }
    }

    private static void clearDownloadSelectionForSoftDeleted(Language language,
                                                             LanguageRepository languageRepository) {
        boolean changed = false;
        if (language.isSelected()) {
            language.setSelected(false);
            changed = true;
        }
        if (language.isSelectedForDownload()) {
            language.setSelectedForDownload(false);
            changed = true;
        }
        if (changed) {
            languageRepository.save(language);
        }
    }

    static void removeSoftDeletedFromDownloadList(List<Language> downloadLanguages,
                                                  Set<String> softDeletedUuids) {
        if (softDeletedUuids.isEmpty()) {
            return;
        }
        for (int i = downloadLanguages.size() - 1; i >= 0; --i) {
            String uuid = downloadLanguages.get(i).getUuid();
            if (uuid != null && softDeletedUuids.contains(uuid)) {
                downloadLanguages.remove(i);
            }
        }
    }
}
