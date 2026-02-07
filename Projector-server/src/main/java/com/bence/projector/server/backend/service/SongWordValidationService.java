package com.bence.projector.server.backend.service;

import com.bence.projector.common.dto.RejectedWordSuggestion;
import com.bence.projector.common.dto.ReviewedWordStatusDTO;
import com.bence.projector.common.dto.SongWordValidationResult;
import com.bence.projector.common.dto.WordWithStatus;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.utils.NormalizedWordBunchMap;
import com.bence.projector.server.utils.UnicodeTextNormalizer;
import com.bence.projector.server.utils.models.NormalizedWordBunch;
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

    private static final int MAX_ALTERNATIVE_SUGGESTIONS = 5;

    private final ReviewedWordService reviewedWordService;
    private final NormalizedWordBunchCacheService normalizedWordBunchCacheService;

    @Autowired
    public SongWordValidationService(ReviewedWordService reviewedWordService,
                                     NormalizedWordBunchCacheService normalizedWordBunchCacheService) {
        this.reviewedWordService = reviewedWordService;
        this.normalizedWordBunchCacheService = normalizedWordBunchCacheService;
    }

    public SongWordValidationResult validateWords(Song song) {
        if (!isValidSong(song)) {
            return createEmptyValidationResult();
        }

        Language language = song.getLanguage();
        Collection<String> songWords = getSongWords(song);
        List<ReviewedWord> allReviewedWords = reviewedWordService.findAllByLanguage(language);

        Map<String, ReviewedWord> reviewedWordMap = buildReviewedWordMap(allReviewedWords);
        NormalizedWordBunchMap normalizedWordBunchMap = buildNormalizedWordBunchMap(language);

        WordCategories categories = categorizeWords(songWords, reviewedWordMap, normalizedWordBunchMap, allReviewedWords);

        return new SongWordValidationResult(
                categories.unreviewedWords,
                categories.bannedWords,
                categories.rejectedWords,
                categories.hasIssues(),
                categories.wordsWithStatus
        );
    }

    private boolean isValidSong(Song song) {
        return song != null && song.getLanguage() != null;
    }

    private SongWordValidationResult createEmptyValidationResult() {
        return new SongWordValidationResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), false, new ArrayList<>());
    }

    private Map<String, ReviewedWord> buildReviewedWordMap(List<ReviewedWord> allReviewedWords) {
        Map<String, ReviewedWord> map = new HashMap<>();
        for (ReviewedWord reviewedWord : allReviewedWords) {
            String word = reviewedWord.getWord();
            if (word != null) {
                map.putIfAbsent(word, reviewedWord);
            }
        }
        return map;
    }

    private NormalizedWordBunchMap buildNormalizedWordBunchMap(Language language) {
        NormalizedWordBunchMap map = new NormalizedWordBunchMap();
        // Use cached word bunches instead of recomputing
        map.populateFromWordBunches(normalizedWordBunchCacheService.getAllWordBunchesForLanguage(language));
        return map;
    }

    private Map<String, Integer> buildCountInSongMap(Collection<String> songWords) {
        Map<String, Integer> countInSong = new HashMap<>();
        for (String word : songWords) {
            countInSong.merge(word, 1, Integer::sum);
        }
        return countInSong;
    }

    private WordCategories categorizeWords(Collection<String> songWords,
                                           Map<String, ReviewedWord> reviewedWordMap,
                                           NormalizedWordBunchMap normalizedWordBunchMap,
                                           List<ReviewedWord> allReviewedWords) {
        WordCategories categories = new WordCategories();
        Set<String> processedWords = new HashSet<>();
        Map<String, Integer> countInSongMap = buildCountInSongMap(songWords);

        for (String word : songWords) {
            if (!processedWords.contains(word)) {
                processedWords.add(word);
                categorizeWord(word, reviewedWordMap, normalizedWordBunchMap,
                        countInSongMap, allReviewedWords, categories);
            }
        }

        return categories;
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
                                Map<String, ReviewedWord> reviewedWordMap,
                                NormalizedWordBunchMap normalizedWordBunchMap,
                                Map<String, Integer> countInSongMap,
                                List<ReviewedWord> allReviewedWords,
                                WordCategories categories) {
        // ReviewedWord map is keyed by lowercase normalized word
        ReviewedWord reviewedWord = reviewedWordMap.get(word);
        Integer countInSong = countInSongMap.get(word);
        int countInAllSongs = getCountInAllSongs(normalizedWordBunchMap, word);

        if (reviewedWord == null) {
            categories.unreviewedWords.add(word);
            categories.wordsWithStatus.add(new WordWithStatus(word, ReviewedWordStatusDTO.UNREVIEWED, null, countInSong, countInAllSongs));
        } else {
            com.bence.projector.server.backend.model.ReviewedWordStatus status = reviewedWord.getStatus();
            if (status == com.bence.projector.server.backend.model.ReviewedWordStatus.BANNED) {
                categories.bannedWords.add(word);
                categories.wordsWithStatus.add(new WordWithStatus(word, ReviewedWordStatusDTO.BANNED, null, countInSong, countInAllSongs));
            } else if (status == com.bence.projector.server.backend.model.ReviewedWordStatus.REJECTED) {
                RejectedWordSuggestion suggestion = findSuggestionsForRejectedWord(word, normalizedWordBunchMap, allReviewedWords);
                categories.rejectedWords.add(suggestion);
                List<String> suggestions = new ArrayList<>();
                if (suggestion.getPrimarySuggestion() != null) {
                    suggestions.add(suggestion.getPrimarySuggestion());
                }
                if (suggestion.getAlternativeSuggestions() != null) {
                    suggestions.addAll(suggestion.getAlternativeSuggestions());
                }
                categories.wordsWithStatus.add(new WordWithStatus(word, ReviewedWordStatusDTO.REJECTED, suggestions, countInSong, countInAllSongs));
            } else {
                // For accepted words, include category and notes
                // For context-specific words, include contextCategory, contextDescription, and notes
                String category = null;
                String notes = null;
                String contextCategory = null;
                String contextDescription = null;
                if (status == com.bence.projector.server.backend.model.ReviewedWordStatus.ACCEPTED) {
                    category = reviewedWord.getCategory();
                    notes = reviewedWord.getNotes();
                } else if (status == com.bence.projector.server.backend.model.ReviewedWordStatus.CONTEXT_SPECIFIC) {
                    contextCategory = reviewedWord.getContextCategory();
                    contextDescription = reviewedWord.getContextDescription();
                    notes = reviewedWord.getNotes();
                }
                categories.wordsWithStatus.add(new WordWithStatus(word, ReviewedWordStatusDTO.fromValue(status.name()), null, countInSong, countInAllSongs, category, notes, contextCategory, contextDescription));
            }
        }
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
                ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC
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
