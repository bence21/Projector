package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.utils.models.NormalizedWordBunch;

import java.util.List;

/**
 * Service for caching normalized word bunches by language.
 * Provides efficient access to normalized word bunches with in-memory caching.
 */
public interface NormalizedWordBunchCacheService {

    /**
     * Gets all normalized word bunches for the given language.
     * Returns cached value if available, otherwise computes and caches the result.
     *
     * @param language the language to get word bunches for
     * @return list of normalized word bunches for the language
     */
    List<NormalizedWordBunch> getAllWordBunchesForLanguage(Language language);

    /**
     * Clears the cache for the specified language.
     *
     * @param languageUuid the UUID of the language to clear cache for
     */
    void clearCacheForLanguage(String languageUuid);

    /**
     * Updates the cache for a language with new word bunches.
     * This is used when word bunches are modified and need to be merged.
     *
     * @param languageUuid the UUID of the language
     * @param wordBunches  the updated word bunches to cache
     */
    void updateCacheForLanguage(String languageUuid, List<NormalizedWordBunch> wordBunches);
}
