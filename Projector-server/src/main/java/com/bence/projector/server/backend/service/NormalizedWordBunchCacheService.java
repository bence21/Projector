package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.utils.models.NormalizedWordBunch;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service for caching normalized word bunches by language.
 * Provides efficient access to normalized word bunches with in-memory caching.
 */
public interface NormalizedWordBunchCacheService {

    /**
     * Gets the set of normalized reviewed words for the given language.
     * Returns cached value if available, otherwise loads from DB and caches the result.
     *
     * @param language the language to get reviewed words for
     * @return set of normalized word strings for the language
     */
    Set<String> getReviewedWordsSetForLanguage(Language language);

    /**
     * Gets the map of word to ReviewedWord for the given language.
     * Returns cached value if available, otherwise loads from DB and caches the result.
     *
     * @param language the language to get reviewed words for
     * @return map of word string to ReviewedWord
     */
    Map<String, ReviewedWord> getReviewedWordMapForLanguage(Language language);

    /**
     * Gets all normalized word bunches for the given language.
     * Returns cached value if available, otherwise computes and caches the result.
     *
     * @param language the language to get word bunches for
     * @return list of normalized word bunches for the language
     */
    List<NormalizedWordBunch> getAllWordBunchesForLanguage(Language language);

    /**
     * Adds a ReviewedWord to the reviewed words caches (set and map) for the given language.
     * No-op if the cache for this language is not yet populated.
     *
     * @param language     the language
     * @param reviewedWord the reviewed word to add
     */
    void addReviewedWordToCache(Language language, ReviewedWord reviewedWord);

    /**
     * Removes a ReviewedWord from the reviewed words caches (set and map) for the given language.
     * No-op if the cache for this language is not yet populated.
     *
     * @param language     the language
     * @param reviewedWord the reviewed word to remove
     */
    void removeReviewedWordFromCache(Language language, ReviewedWord reviewedWord);

    /**
     * Adds a normalized word to the reviewed words set cache for the given language.
     * No-op if the cache for this language is not yet populated.
     *
     * @param language       the language
     * @param normalizedWord the normalized word to add
     */
    void addToReviewedWordsSet(Language language, String normalizedWord);

    /**
     * Removes a normalized word from the reviewed words set cache for the given language.
     * No-op if the cache for this language is not yet populated.
     *
     * @param language       the language
     * @param normalizedWord the normalized word to remove
     */
    void removeFromReviewedWordsSet(Language language, String normalizedWord);

    /**
     * Adds a ReviewedWord to the reviewed word map cache for the given language.
     * No-op if the cache for this language is not yet populated.
     *
     * @param language     the language
     * @param reviewedWord the reviewed word to add
     */
    void addToReviewedWordMap(Language language, ReviewedWord reviewedWord);

    /**
     * Removes a word from the reviewed word map cache for the given language.
     * No-op if the cache for this language is not yet populated.
     *
     * @param language the language
     * @param word     the word (map key) to remove
     */
    void removeFromReviewedWordMap(Language language, String word);

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
