package com.bence.projector.server.backend.service.inreview;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.utils.SongLyricCharCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps In-review songs whose verse text length is at most {@code maxLyricChars}.
 */
public final class InReviewSongShortLyricsFilter {

    private InReviewSongShortLyricsFilter() {
    }

    public static List<Song> filter(List<Song> songs, int maxLyricChars) {
        List<Song> matched = new ArrayList<>();
        for (Song song : songs) {
            if (SongLyricCharCounter.isShortLyrics(song, maxLyricChars)) {
                matched.add(song);
            }
        }
        return matched;
    }
}
