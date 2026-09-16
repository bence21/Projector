package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;

import java.util.List;

/**
 * Counts lyric characters as the sum of verse body lengths (title excluded).
 */
public final class SongLyricCharCounter {

    public static final int SHORT_LYRIC_MAX_CHARS = 300;

    private SongLyricCharCounter() {
    }

    public static int count(Song song) {
        if (song == null) {
            return 0;
        }
        List<SongVerse> verses = song.getVerses();
        if (verses == null) {
            return 0;
        }
        int total = 0;
        for (SongVerse verse : verses) {
            String text = verse.getText();
            if (text != null) {
                total += text.length();
            }
        }
        return total;
    }

    public static boolean isShortLyrics(Song song, int maxChars) {
        return count(song) <= maxChars;
    }
}
