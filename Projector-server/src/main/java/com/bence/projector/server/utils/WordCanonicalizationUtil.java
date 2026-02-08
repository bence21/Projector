package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.service.ReviewedWordService;
import com.bence.projector.server.backend.service.SongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WordCanonicalizationUtil {

    private static final Logger log = LoggerFactory.getLogger(WordCanonicalizationUtil.class);

    /**
     * Canonicalizes Unicode for all reviewed words in the database using {@link UnicodeTextNormalizer#canonicalizeUnicode(String)}.
     * Updates the {@code word} field with the canonicalized version, which will also automatically update
     * the {@code normalizedWord} field via the setter.
     *
     * @param reviewedWordService service to load and save reviewed words
     */
    public static void canonicalizeAllWords(ReviewedWordService reviewedWordService) {
        List<ReviewedWord> words = reviewedWordService.findAll();
        log.info("Canonicalizing Unicode for {} words...", words.size());

        List<ReviewedWord> wordsToSave = new ArrayList<>();
        for (ReviewedWord rw : words) {
            String word = rw.getWord();
            if (word == null) {
                continue;
            }
            String canonicalized = UnicodeTextNormalizer.canonicalizeUnicode(word);
            if (canonicalized != null && !canonicalized.equals(word)) {
                // setWord() will automatically update normalizedWord via normalizeAccents()
                rw.setWord(canonicalized);
                wordsToSave.add(rw);
            }
        }

        reviewedWordService.save(wordsToSave);
        log.info("Completed Unicode canonicalization of {} words.", wordsToSave.size());
    }

    /**
     * Canonicalizes Unicode for all songs in the database.
     * Updates song titles and verse texts with canonicalized versions.
     * This ensures that all song data is stored in canonical Unicode form (NFC + zero-width removal).
     *
     * @param songService service to load and save songs
     */
    public static void canonicalizeAllSongs(SongService songService) {
        List<Song> songs = songService.findAll();
        log.info("Canonicalizing Unicode for {} songs...", songs.size());

        List<Song> songsToSave = new ArrayList<>();
        for (Song song : songs) {
            boolean songChanged = canonicalizeSongTitle(song);
            songChanged |= canonicalizeSongVerses(song);

            if (songChanged) {
                songsToSave.add(song);
            }
        }

        songService.save(songsToSave);
        log.info("Completed Unicode canonicalization of {} songs.", songsToSave.size());
    }

    /**
     * Canonicalizes the title of a song.
     *
     * @param song the song whose title should be canonicalized
     * @return true if the title was changed, false otherwise
     */
    private static boolean canonicalizeSongTitle(Song song) {
        String title = song.getTitle();
        if (title == null) {
            return false;
        }
        String canonicalizedTitle = UnicodeTextNormalizer.canonicalizeUnicode(title);
        if (canonicalizedTitle != null && !canonicalizedTitle.equals(title)) {
            song.setTitle(canonicalizedTitle);
            return true;
        }
        return false;
    }

    /**
     * Canonicalizes the text of all verses in a song.
     *
     * @param song the song whose verses should be canonicalized
     * @return true if any verse text was changed, false otherwise
     */
    private static boolean canonicalizeSongVerses(Song song) {
        List<SongVerse> verses = song.getVerses();
        if (verses == null) {
            return false;
        }
        boolean changed = false;
        for (SongVerse verse : verses) {
            String text = verse.getText();
            if (text == null) {
                continue;
            }
            String canonicalizedText = UnicodeTextNormalizer.canonicalizeUnicode(text);
            if (canonicalizedText != null && !canonicalizedText.equals(text)) {
                verse.setText(canonicalizedText);
                changed = true;
            }
        }
        return changed;
    }
}
