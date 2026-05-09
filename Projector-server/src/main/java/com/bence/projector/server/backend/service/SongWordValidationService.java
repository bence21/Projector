package com.bence.projector.server.backend.service;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.common.dto.RejectedWordSuggestion;
import com.bence.projector.common.dto.ReviewedWordStatusDTO;
import com.bence.projector.common.dto.SongWordValidationResult;
import com.bence.projector.common.dto.WordWithStatus;
import com.bence.projector.server.api.assembler.LanguageAssembler;
import com.bence.projector.server.backend.model.ForeignLanguageType;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.utils.NormalizedWordBunchMap;
import com.bence.projector.server.utils.UnicodeTextNormalizer;
import com.bence.projector.server.utils.models.NormalizedWordBunch;
import com.bence.projector.server.utils.models.SongWord;
import com.bence.projector.server.utils.models.WordBunch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bence.projector.server.utils.SetLanguages.getSongWords;
import static com.bence.projector.server.utils.UnicodeTextNormalizer.canonicalizeUnicode;

@Service
public class SongWordValidationService {

    private static final MixedLanguageMetrics EMPTY_MIXED_LANGUAGE_METRICS =
            new MixedLanguageMetrics(false, 0, 0, 0.0d, List.of());

    private static final int MAX_ALTERNATIVE_SUGGESTIONS = 5;
    private static final double MIXED_LANGUAGE_RATIO_THRESHOLD = 0.15d;
    private static final int MIXED_LANGUAGE_WORD_COUNT_THRESHOLD = 10;

    private final NormalizedWordBunchCacheService normalizedWordBunchCacheService;
    private final LanguageAssembler languageAssembler;
    private final ReviewedWordService reviewedWordService;

    @Autowired
    public SongWordValidationService(NormalizedWordBunchCacheService normalizedWordBunchCacheService,
                                     LanguageAssembler languageAssembler,
                                     ReviewedWordService reviewedWordService) {
        this.normalizedWordBunchCacheService = normalizedWordBunchCacheService;
        this.languageAssembler = languageAssembler;
        this.reviewedWordService = reviewedWordService;
    }

    public SongWordValidationResult validateWords(Song song) {
        return validateWords(song, SongWordValidationOptions.DEFAULT);
    }

    public SongWordValidationResult validateWords(Song song, SongWordValidationOptions options) {
        if (options == null) {
            options = SongWordValidationOptions.DEFAULT;
        }
        if (!isValidSong(song)) {
            return createEmptyValidationResult();
        }

        Language language = song.getLanguage();
        Collection<SongWord> songWords = getSongWords(song);
        Map<String, ReviewedWord> reviewedWordMap = normalizedWordBunchCacheService.getReviewedWordMapForLanguage(language);
        List<ReviewedWord> allReviewedWordsForSuggestions = options.includeWordSuggestions()
                ? new ArrayList<>(reviewedWordMap.values())
                : null;
        NormalizedWordBunchMap normalizedWordBunchMap = buildNormalizedWordBunchMap(language);

        WordCategories categories = categorizeWords(songWords, reviewedWordMap, normalizedWordBunchMap,
                allReviewedWordsForSuggestions, options);
        MixedLanguageMetrics mixedLanguageMetrics = options.includeMixedLanguageAnalysis()
                ? computeMixedLanguageMetrics(categories.wordsWithStatus, categories.unreviewedWords, language)
                : EMPTY_MIXED_LANGUAGE_METRICS;

        return new SongWordValidationResult(
                categories.unreviewedWords,
                categories.bannedWords,
                categories.rejectedWords,
                categories.hasIssues(),
                categories.wordsWithStatus,
                mixedLanguageMetrics.hasWarning,
                mixedLanguageMetrics.foreignWordCount,
                mixedLanguageMetrics.totalReviewedWordCount,
                mixedLanguageMetrics.foreignWordRatio,
                mixedLanguageMetrics.foreignLanguages
        );
    }

    private boolean isValidSong(Song song) {
        return song != null && song.getLanguage() != null;
    }

    private SongWordValidationResult createEmptyValidationResult() {
        return new SongWordValidationResult(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                false,
                new ArrayList<>(),
                false,
                0,
                0,
                0.0d,
                new ArrayList<>()
        );
    }

    private NormalizedWordBunchMap buildNormalizedWordBunchMap(Language language) {
        NormalizedWordBunchMap map = new NormalizedWordBunchMap();
        // Use cached word bunches instead of recomputing
        map.populateFromWordBunches(normalizedWordBunchCacheService.getAllWordBunchesForLanguage(language));
        return map;
    }

    private Map<String, Integer> buildCountInSongMap(Collection<SongWord> songWords) {
        Map<String, Integer> countInSong = new HashMap<>();
        for (SongWord songWord : songWords) {
            countInSong.merge(songWord.getWord(), 1, Integer::sum);
        }
        return countInSong;
    }

    /**
     * For each word, true iff every occurrence in songWords is auto-capitalized
     * (first-in-sentence or first-in-line).
     */
    private Map<String, Boolean> buildAllOccurrencesAutoCapitalizedMap(Collection<SongWord> songWords) {
        Map<String, Boolean> map = new HashMap<>();
        for (SongWord songWord : songWords) {
            map.merge(songWord.getWord(), songWord.isFirstWordInSentence() || songWord.isFirstWordInLine(), Boolean::logicalAnd);
        }
        return map;
    }

    private WordCategories categorizeWords(Collection<SongWord> songWords,
                                           Map<String, ReviewedWord> reviewedWordMap,
                                           NormalizedWordBunchMap normalizedWordBunchMap,
                                           List<ReviewedWord> allReviewedWordsForSuggestions,
                                           SongWordValidationOptions options) {
        WordCategories categories = new WordCategories();
        Set<String> processedWords = new HashSet<>();
        Map<String, Integer> countInSongMap = buildCountInSongMap(songWords);
        Map<String, Boolean> allOccurrencesAutoCapitalized = buildAllOccurrencesAutoCapitalizedMap(songWords);

        for (SongWord songWord : songWords) {
            String word = songWord.getWord();
            if (!processedWords.contains(word)) {
                processedWords.add(word);
                boolean allAutoCapitalized = Boolean.TRUE.equals(allOccurrencesAutoCapitalized.get(word));
                categorizeWord(word, allAutoCapitalized, reviewedWordMap, normalizedWordBunchMap,
                        countInSongMap, allReviewedWordsForSuggestions, categories, options);
            }
        }

        return categories;
    }

    /**
     * Returns true if the word is in capitalized first-word form (only first character uppercase, rest lowercase).
     * Used to treat e.g. "Hello" as reviewed when "hello" is already reviewed.
     */
    private boolean isCapitalizedFirstWordForm(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        return Character.isUpperCase(word.charAt(0))
                && word.substring(1).equals(word.substring(1).toLowerCase());
    }

    /**
     * If the word is in capitalized first-word form and its lowercase is already reviewed (non-blocking),
     * adds it to wordsWithStatus with the lowercase's status and returns true. Otherwise, returns false.
     */
    private boolean tryTreatCapitalizedAsReviewed(String word,
                                                  Map<String, ReviewedWord> reviewedWordMap,
                                                  Integer countInSong,
                                                  int countInAllSongs,
                                                  WordCategories categories) {
        if (!isCapitalizedFirstWordForm(word)) {
            return false;
        }
        ReviewedWord reviewedWordLower = reviewedWordMap.get(word.toLowerCase());
        if (reviewedWordLower == null) {
            return false;
        }
        ReviewedWordStatus statusLower = reviewedWordLower.getStatus();
        if (statusLower == ReviewedWordStatus.BANNED || statusLower == ReviewedWordStatus.REJECTED) {
            return false;
        }
        WordWithStatus wordWithStatus = addWordWithStatusFromReviewedWord(word, reviewedWordLower, countInSong, countInAllSongs, categories, true);
        wordWithStatus.setAllOccurrencesAutoCapitalized(true);
        return true;
    }

    /**
     * Returns the total count of the given word across all songs in the bunches.
     */
    private int getCountInAllSongs(NormalizedWordBunchMap normalizedWordBunchMap, String word) {
        NormalizedWordBunch nwb = normalizedWordBunchMap.get(word);
        if (nwb == null) {
            return 0;
        }
        return nwb.getWordBunches().stream()
                .filter(wb -> word.equals(wb.getWord()))
                .mapToInt(WordBunch::getCount)
                .sum();
    }

    private void categorizeWord(String word,
                                boolean allOccurrencesAutoCapitalized,
                                Map<String, ReviewedWord> reviewedWordMap,
                                NormalizedWordBunchMap normalizedWordBunchMap,
                                Map<String, Integer> countInSongMap,
                                List<ReviewedWord> allReviewedWordsForSuggestions,
                                WordCategories categories,
                                SongWordValidationOptions options) {
        // ReviewedWord map is keyed by lowercase normalized word
        ReviewedWord reviewedWord = reviewedWordMap.get(word);
        Integer countInSong = countInSongMap.get(word);
        int countInAllSongs = getCountInAllSongs(normalizedWordBunchMap, word);

        WordWithStatus added;
        if (reviewedWord == null) {
            if (allOccurrencesAutoCapitalized && tryTreatCapitalizedAsReviewed(word, reviewedWordMap, countInSong, countInAllSongs, categories)) {
                return;
            }
            categories.unreviewedWords.add(word);
            List<String> suggestions = null;
            if (options.includeWordSuggestions()) {
                RejectedWordSuggestion suggestion = findSuggestionsForRejectedWord(word, normalizedWordBunchMap,
                        allReviewedWordsForSuggestions);
                suggestions = extractSuggestions(suggestion);
            }
            added = new WordWithStatus(word, ReviewedWordStatusDTO.UNREVIEWED, suggestions, countInSong, countInAllSongs);
            categories.wordsWithStatus.add(added);
        } else {
            com.bence.projector.server.backend.model.ReviewedWordStatus status = reviewedWord.getStatus();
            if (status == com.bence.projector.server.backend.model.ReviewedWordStatus.BANNED) {
                categories.bannedWords.add(word);
                added = new WordWithStatus(word, ReviewedWordStatusDTO.BANNED, null, countInSong, countInAllSongs);
                categories.wordsWithStatus.add(added);
            } else if (status == com.bence.projector.server.backend.model.ReviewedWordStatus.REJECTED) {
                RejectedWordSuggestion suggestion;
                List<String> suggestions;
                if (options.includeWordSuggestions()) {
                    suggestion = findSuggestionsForRejectedWord(word, normalizedWordBunchMap, allReviewedWordsForSuggestions);
                    suggestions = extractSuggestions(suggestion);
                } else {
                    suggestion = new RejectedWordSuggestion(word, null, null);
                    suggestions = null;
                }
                categories.rejectedWords.add(suggestion);
                added = new WordWithStatus(word, ReviewedWordStatusDTO.REJECTED, suggestions, countInSong, countInAllSongs);
                categories.wordsWithStatus.add(added);
            } else if (status == com.bence.projector.server.backend.model.ReviewedWordStatus.NOT_SURE) {
                added = new WordWithStatus(word, ReviewedWordStatusDTO.NOT_SURE, null, countInSong, countInAllSongs);
                categories.wordsWithStatus.add(added);
            } else {
                added = addWordWithStatusFromReviewedWord(word, reviewedWord, countInSong, countInAllSongs, categories, false);
            }
        }
        added.setAllOccurrencesAutoCapitalized(allOccurrencesAutoCapitalized);
    }

    /**
     * Extracts category/notes/context metadata from a reviewed word (for ACCEPTED or CONTEXT_SPECIFIC)
     * and adds a WordWithStatus to categories.
     *
     * @param inheritedFromCapitalizedReview true when the word is treated as reviewed only via the capitalized-word rule
     * @return the created WordWithStatus (caller may set allOccurrencesAutoCapitalized)
     */
    private WordWithStatus addWordWithStatusFromReviewedWord(String word, ReviewedWord reviewedWord,
                                                             Integer countInSong, int countInAllSongs,
                                                             WordCategories categories,
                                                             boolean inheritedFromCapitalizedReview) {
        ReviewedWordStatus status = reviewedWord.getStatus();
        String category = null;
        String notes = null;
        String contextCategory = null;
        String contextDescription = null;
        LanguageDTO sourceLanguageDto = null;
        Integer foreignLanguageTypeOrdinal = null;
        if (status == ReviewedWordStatus.ACCEPTED) {
            category = reviewedWord.getCategory();
            notes = reviewedWord.getNotes();
            if (reviewedWord.getSourceLanguage() != null) {
                sourceLanguageDto = languageAssembler.createDto(reviewedWord.getSourceLanguage());
            }
            if (reviewedWord.getForeignLanguageType() != null) {
                foreignLanguageTypeOrdinal = reviewedWord.getForeignLanguageType().ordinal();
            }
        } else if (status == ReviewedWordStatus.CONTEXT_SPECIFIC) {
            contextCategory = reviewedWord.getContextCategory();
            contextDescription = reviewedWord.getContextDescription();
            notes = reviewedWord.getNotes();
        }
        WordWithStatus wordWithStatus = new WordWithStatus(word, ReviewedWordStatusDTO.fromValue(status.name()), null, countInSong, countInAllSongs, category, notes, contextCategory, contextDescription, sourceLanguageDto, foreignLanguageTypeOrdinal);
        wordWithStatus.setInheritedFromCapitalizedReview(inheritedFromCapitalizedReview);
        categories.wordsWithStatus.add(wordWithStatus);
        return wordWithStatus;
    }

    private static class WordCategories {
        final List<String> unreviewedWords = new ArrayList<>();
        final List<String> bannedWords = new ArrayList<>();
        final List<RejectedWordSuggestion> rejectedWords = new ArrayList<>();
        final List<WordWithStatus> wordsWithStatus = new ArrayList<>();

        boolean hasIssues() {
            return !unreviewedWords.isEmpty() || !bannedWords.isEmpty() || !rejectedWords.isEmpty();
        }
    }

    private MixedLanguageMetrics computeMixedLanguageMetrics(List<WordWithStatus> wordsWithStatus,
                                                             List<String> unreviewedWords,
                                                             Language songLanguage) {
        if (wordsWithStatus == null || wordsWithStatus.isEmpty()) {
            return buildMetricsFromCounts(0, 0, new HashSet<>());
        }

        int foreignWordCount = 0;
        int totalReviewedWordCount = 0;
        Set<String> foreignLanguageSet = new HashSet<>();

        for (WordWithStatus wordWithStatus : wordsWithStatus) {
            ReviewedWordStatusDTO status = wordWithStatus.getStatus();
            if (!isReviewedStatus(status)) {
                continue;
            }
            totalReviewedWordCount++;
            if (isExplicitForeignWord(wordWithStatus)) {
                foreignWordCount++;
                addForeignLanguageLabel(foreignLanguageSet, wordWithStatus.getSourceLanguage());
            }
        }

        if (unreviewedWords != null && songLanguage != null) {
            for (String unreviewedWord : unreviewedWords) {
                List<Language> sourceLanguages = reviewedWordService.detectSourceLanguages(unreviewedWord, songLanguage);
                if (sourceLanguages == null || sourceLanguages.isEmpty()) {
                    continue;
                }
                foreignWordCount++;
                for (Language sourceLanguage : sourceLanguages) {
                    if (sourceLanguage == null) {
                        continue;
                    }
                    String englishName = sourceLanguage.getEnglishName();
                    String nativeName = sourceLanguage.getNativeName();
                    if (englishName != null && !englishName.trim().isEmpty()) {
                        foreignLanguageSet.add(englishName.trim());
                    } else if (nativeName != null && !nativeName.trim().isEmpty()) {
                        foreignLanguageSet.add(nativeName.trim());
                    }
                }
            }
        }

        return buildMetricsFromCounts(foreignWordCount, totalReviewedWordCount, foreignLanguageSet);
    }

    private MixedLanguageMetrics buildMetricsFromCounts(int foreignWordCount, int totalReviewedWordCount, Set<String> foreignLanguageSet) {
        List<String> foreignLanguages = new ArrayList<>(foreignLanguageSet);
        foreignLanguages.sort(String::compareToIgnoreCase);

        double foreignWordRatio = totalReviewedWordCount == 0
                ? 0.0d
                : (double) foreignWordCount / totalReviewedWordCount;
        boolean hasWarning = foreignWordRatio > MIXED_LANGUAGE_RATIO_THRESHOLD
                || foreignWordCount >= MIXED_LANGUAGE_WORD_COUNT_THRESHOLD;

        return new MixedLanguageMetrics(hasWarning, foreignWordCount, totalReviewedWordCount, foreignWordRatio, foreignLanguages);
    }

    private boolean isReviewedStatus(ReviewedWordStatusDTO status) {
        if (status == null) {
            return false;
        }
        return status == ReviewedWordStatusDTO.REVIEWED_GOOD
                || status == ReviewedWordStatusDTO.ACCEPTED
                || status == ReviewedWordStatusDTO.CONTEXT_SPECIFIC
                || status == ReviewedWordStatusDTO.AUTO_ACCEPTED_FROM_PUBLIC
                || status == ReviewedWordStatusDTO.AUTO_ACCEPTED_FROM_BIBLE;
    }

    private boolean isExplicitForeignWord(WordWithStatus wordWithStatus) {
        if (wordWithStatus == null) {
            return false;
        }
        Integer foreignLanguageType = wordWithStatus.getForeignLanguageType();
        return foreignLanguageType != null && foreignLanguageType == ForeignLanguageType.FOREIGN.ordinal();
    }

    private void addForeignLanguageLabel(Set<String> labels, LanguageDTO sourceLanguage) {
        if (labels == null || sourceLanguage == null) {
            return;
        }
        String englishName = sourceLanguage.getEnglishName();
        String nativeName = sourceLanguage.getNativeName();
        if (englishName != null && !englishName.trim().isEmpty()) {
            labels.add(englishName.trim());
            return;
        }
        if (nativeName != null && !nativeName.trim().isEmpty()) {
            labels.add(nativeName.trim());
        }
    }

    private record MixedLanguageMetrics(boolean hasWarning, int foreignWordCount, int totalReviewedWordCount,
                                        double foreignWordRatio, List<String> foreignLanguages) {
    }

    /**
     * Extracts suggestions from a RejectedWordSuggestion object into a list of strings.
     * Includes both primary and alternative suggestions.
     */
    private List<String> extractSuggestions(RejectedWordSuggestion suggestion) {
        List<String> suggestions = new ArrayList<>();
        if (suggestion.getPrimarySuggestion() != null) {
            suggestions.add(suggestion.getPrimarySuggestion());
        }
        if (suggestion.getAlternativeSuggestions() != null) {
            suggestions.addAll(suggestion.getAlternativeSuggestions());
        }
        return suggestions;
    }

    private RejectedWordSuggestion findSuggestionsForRejectedWord(
            String word,
            NormalizedWordBunchMap normalizedWordBunchMap,
            List<ReviewedWord> allReviewedWords) {

        CanonicalizedWordResult canonicalizedResult = canonicalizeWordAndPrimarySuggestion(word, normalizedWordBunchMap);
        String canonicalizedOriginalWord = canonicalizedResult.canonicalizedWord;
        String primarySuggestion = canonicalizedResult.primarySuggestion;

        SuggestionResult suggestionResult = findSimilarGoodWords(
                canonicalizedOriginalWord, word, primarySuggestion, allReviewedWords);

        primarySuggestion = canonicalizeSuggestionResult(suggestionResult, primarySuggestion);

        limitSuggestions(suggestionResult.alternativeSuggestions);

        return new RejectedWordSuggestion(word, primarySuggestion, suggestionResult.alternativeSuggestions);
    }

    private CanonicalizedWordResult canonicalizeWordAndPrimarySuggestion(String word, NormalizedWordBunchMap normalizedWordBunchMap) {
        // Canonicalize Unicode for the original word for consistent handling
        String canonicalizedOriginalWord = canonicalizeUnicode(word);

        String primarySuggestion = normalizedWordBunchMap.getBestWord(word);
        if (primarySuggestion != null) {
            primarySuggestion = canonicalizeUnicode(primarySuggestion);
        }

        return new CanonicalizedWordResult(canonicalizedOriginalWord, primarySuggestion);
    }

    private String canonicalizeSuggestionResult(SuggestionResult suggestionResult, String primarySuggestion) {
        if (suggestionResult.primarySuggestion != null) {
            primarySuggestion = canonicalizeUnicode(suggestionResult.primarySuggestion);
        }

        // Canonicalize Unicode for all alternative suggestions
        suggestionResult.alternativeSuggestions.replaceAll(UnicodeTextNormalizer::canonicalizeUnicode);

        return primarySuggestion;
    }

    private record CanonicalizedWordResult(String canonicalizedWord, String primarySuggestion) {
    }

    private SuggestionResult findSimilarGoodWords(String originalWord, String normalizedWord,
                                                  String existingPrimarySuggestion,
                                                  List<ReviewedWord> allReviewedWords) {
        SuggestionResult result = new SuggestionResult();
        Set<ReviewedWordStatus> goodStatuses = getGoodStatuses();

        for (ReviewedWord reviewedWord : allReviewedWords) {
            if (isGoodStatusWord(reviewedWord, goodStatuses) &&
                    isSimilarNormalizedWord(normalizedWord, reviewedWord)) {
                String reviewedWordText = reviewedWord.getWord();
                if (isValidSuggestion(originalWord, reviewedWordText, existingPrimarySuggestion, result)) {
                    if (existingPrimarySuggestion == null && result.primarySuggestion == null) {
                        result.primarySuggestion = reviewedWordText;
                    } else {
                        result.alternativeSuggestions.add(reviewedWordText);
                    }
                }
            }
        }

        return result;
    }

    private static class SuggestionResult {
        String primarySuggestion;
        final List<String> alternativeSuggestions = new ArrayList<>();
    }

    private Set<ReviewedWordStatus> getGoodStatuses() {
        return Set.of(
                ReviewedWordStatus.REVIEWED_GOOD,
                ReviewedWordStatus.ACCEPTED,
                ReviewedWordStatus.CONTEXT_SPECIFIC,
                ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC,
                ReviewedWordStatus.AUTO_ACCEPTED_FROM_BIBLE
        );
    }

    private boolean isGoodStatusWord(ReviewedWord reviewedWord, Set<ReviewedWordStatus> goodStatuses) {
        return goodStatuses.contains(reviewedWord.getStatus());
    }

    private boolean isSimilarNormalizedWord(String normalizedWord, ReviewedWord reviewedWord) {
        String reviewedNormalized = reviewedWord.getNormalizedWord();
        return reviewedNormalized != null && isSimilar(normalizedWord, reviewedNormalized);
    }

    private boolean isValidSuggestion(String originalWord, String suggestion,
                                      String primarySuggestion, SuggestionResult result) {
        return suggestion != null &&
                !suggestion.equals(originalWord) &&
                !suggestion.equals(primarySuggestion) &&
                !suggestion.equals(result.primarySuggestion) &&
                !result.alternativeSuggestions.contains(suggestion);
    }

    private void limitSuggestions(List<String> suggestions) {
        if (suggestions.size() > MAX_ALTERNATIVE_SUGGESTIONS) {
            suggestions.subList(MAX_ALTERNATIVE_SUGGESTIONS, suggestions.size()).clear();
        }
    }

    private boolean isSimilar(String word1, String word2) {
        if (word1 == null || word2 == null) {
            return false;
        }

        // Simple similarity: same length or length difference <= 2, and at least 70% character match
        int len1 = word1.length();
        int len2 = word2.length();

        if (Math.abs(len1 - len2) > 2) {
            return false;
        }

        // Calculate character similarity
        int matches = 0;
        int minLen = Math.min(len1, len2);
        for (int i = 0; i < minLen; i++) {
            if (word1.charAt(i) == word2.charAt(i)) {
                matches++;
            }
        }

        double similarity = (double) matches / Math.max(len1, len2);
        return similarity >= 0.7;
    }
}
