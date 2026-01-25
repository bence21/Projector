package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.ReviewedWordService;
import com.bence.projector.server.backend.service.SongService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bence.projector.server.utils.SetLanguages.getSongWords;

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
     * @return AutoAcceptResult containing the number of songs processed
     * @throws IllegalArgumentException if language is not found
     */
    public static AutoAcceptResult autoAcceptWordsFromPublicSongs(
            LanguageService languageService,
            ReviewedWordService reviewedWordService,
            SongService songService,
            String languageUuid,
            long minScore) {

        Language language = validateAndGetLanguage(languageService, languageUuid);
        Set<String> existingReviewedWords = getExistingReviewedWords(reviewedWordService, language);

        WordsCollectionResult wordsResult = collectWordsFromHighScoringSongs(songService, language, existingReviewedWords, minScore);

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

    private static Set<String> getExistingReviewedWords(ReviewedWordService reviewedWordService, Language language) {
        return reviewedWordService.findAllByLanguage(language).stream()
                .map(ReviewedWord::getWord)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private record WordsCollectionResult(Set<String> wordsToSave, Set<Song> processedSongs) {
    }

    private static WordsCollectionResult collectWordsFromHighScoringSongs(
            SongService songService,
            Language language,
            Set<String> existingReviewedWords,
            long minScore) {

        // First pass: get all songs for the language and filter to high-scoring public songs
        List<Song> allSongs = songService.findAllByLanguage(language.getUuid());
        Set<Song> highScoringSongs = filterHighScoringSongs(allSongs, minScore);

        // Second pass: collect words from high-scoring songs
        Set<String> wordsToSave = new HashSet<>();
        for (Song song : highScoringSongs) {
            collectWordsFromSong(song, existingReviewedWords, wordsToSave);
        }

        return new WordsCollectionResult(wordsToSave, highScoringSongs);
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
            Set<String> existingReviewedWords,
            Set<String> wordsToSave) {
        Collection<String> songWords = getSongWords(song);
        for (String word : songWords) {
            if (word != null && !word.isEmpty() && !existingReviewedWords.contains(word)) {
                wordsToSave.add(word);
            }
        }
    }

    private static void saveReviewedWords(
            ReviewedWordService reviewedWordService,
            Language language,
            Set<String> wordsToSave) {

        if (wordsToSave == null || wordsToSave.isEmpty()) {
            return;
        }

        // Convert Set to List for bulk save
        List<String> wordsList = new ArrayList<>(wordsToSave);
        
        // Bulk save all new words at once (reviewedBy is null for automatically applied words)
        reviewedWordService.saveBulkNewWords(wordsList, language, null);
    }

}
