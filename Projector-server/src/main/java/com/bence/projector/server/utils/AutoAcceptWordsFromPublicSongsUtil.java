package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.ReviewedWordService;
import com.bence.projector.server.backend.service.SongService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bence.projector.server.utils.SetLanguages.getSongWords;
import static com.bence.projector.server.utils.StringUtils.normalizeAccents;

public class AutoAcceptWordsFromPublicSongsUtil {

    /**
     * Result class to hold statistics about the auto-accept operation.
     */
    public record AutoAcceptResult(int songsProcessed) {
    }

    /**
     * Automatically accepts words from public songs with a score at least equal to minScore.
     * Words are saved with the AUTO_ACCEPTED_FROM_PUBLIC status.
     *
     * @param languageService     Service to retrieve language information
     * @param reviewedWordService Service to save reviewed words
     * @param songService         Service to retrieve songs
     * @param languageUuid        UUID of the language to process
     * @param minScore            Minimum score threshold for songs (default: 50)
     * @param minOccurrences      Minimum occurrence count for a word to be accepted (default: 2)
     * @return AutoAcceptResult containing the number of songs processed
     * @throws IllegalArgumentException if language is not found
     */
    public static AutoAcceptResult autoAcceptWordsFromPublicSongs(
            LanguageService languageService,
            ReviewedWordService reviewedWordService,
            SongService songService,
            String languageUuid,
            long minScore,
            int minOccurrences) {

        Language language = validateAndGetLanguage(languageService, languageUuid);
        ExistingReviewedWords existing = getExistingReviewedWords(reviewedWordService, language);

        WordsCollectionResult wordsResult = collectWordsFromHighScoringSongs(songService, language, existing, minScore, minOccurrences);

        saveReviewedWords(reviewedWordService, language, wordsResult.wordsToSave());

        return new AutoAcceptResult(wordsResult.processedSongs().size());
    }

    private static Language validateAndGetLanguage(LanguageService languageService, String languageUuid) {
        Language language = languageService.findOneByUuid(languageUuid);
        if (language == null) {
            throw new IllegalArgumentException("Language not found with UUID: " + languageUuid);
        }
        return language;
    }

    private record ExistingReviewedWords(Set<String> exactWords, Set<String> normalizedWords) {
    }

    private static ExistingReviewedWords getExistingReviewedWords(ReviewedWordService reviewedWordService, Language language) {
        List<ReviewedWord> all = reviewedWordService.findAllByLanguage(language);
        Set<String> exact = all.stream()
                .map(ReviewedWord::getWord)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> normalized = all.stream()
                .map(ReviewedWord::getNormalizedWord)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        return new ExistingReviewedWords(exact, normalized);
    }

    private record WordsCollectionResult(Map<String, Integer> wordsToSave, Set<Song> processedSongs) {
    }

    private static WordsCollectionResult collectWordsFromHighScoringSongs(
            SongService songService,
            Language language,
            ExistingReviewedWords existing,
            long minScore,
            int minOccurrences) {

        // First pass: get all songs for the language and filter to high-scoring public songs
        List<Song> allSongs = songService.findAllByLanguage(language.getUuid());
        Set<Song> highScoringSongs = filterHighScoringSongs(allSongs, minScore);

        // Second pass: collect words with occurrence counts from high-scoring songs
        Map<String, Integer> wordCounts = new HashMap<>();
        for (Song song : highScoringSongs) {
            collectWordsFromSong(song, existing, wordCounts);
        }

        Map<String, Integer> wordsToSave = filterWordsByQuality(wordCounts, minOccurrences);
        return new WordsCollectionResult(wordsToSave, highScoringSongs);
    }

    private static Map<String, Integer> filterWordsByQuality(Map<String, Integer> wordCounts, int minOccurrences) {
        if (wordCounts == null || minOccurrences <= 0) {
            return wordCounts != null ? wordCounts : new HashMap<>();
        }
        Map<String, Integer> filtered = new HashMap<>();
        for (Map.Entry<String, Integer> e : wordCounts.entrySet()) {
            if (e.getValue() >= minOccurrences) {
                filtered.put(e.getKey(), e.getValue());
            }
        }
        return filtered;
    }

    private static Set<Song> filterHighScoringSongs(List<Song> allSongs, long minScore) {
        Set<Song> highScoringSongs = new HashSet<>();
        for (Song song : allSongs) {
            if (song.isPublic() && song.getScore() >= minScore) {
                highScoringSongs.add(song);
            }
        }
        return highScoringSongs;
    }

    private static void collectWordsFromSong(
            Song song,
            ExistingReviewedWords existing,
            Map<String, Integer> wordCounts) {
        Collection<String> songWords = getSongWords(song);
        for (String word : songWords) {
            if (word == null || word.isEmpty()) {
                continue;
            }
            if (existing.exactWords().contains(word)) {
                continue;
            }
            String normalized = normalizeAccents(word.toLowerCase());
            if (normalized != null && existing.normalizedWords().contains(normalized)) {
                continue;
            }
            wordCounts.merge(word, 1, Integer::sum);
        }
    }

    private static void saveReviewedWords(
            ReviewedWordService reviewedWordService,
            Language language,
            Map<String, Integer> wordsToSave) {

        if (wordsToSave == null || wordsToSave.isEmpty()) {
            return;
        }

        List<String> wordsList = new ArrayList<>(wordsToSave.keySet());
        reviewedWordService.saveBulkNewWords(wordsList, language, null);
    }

}
