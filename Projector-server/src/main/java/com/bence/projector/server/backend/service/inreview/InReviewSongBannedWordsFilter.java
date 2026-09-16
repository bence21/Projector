package com.bence.projector.server.backend.service.inreview;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.service.SongWordValidationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps In-review songs whose lyrics contain a banned or rejected word.
 */
public final class InReviewSongBannedWordsFilter {

    private InReviewSongBannedWordsFilter() {
    }

    public static List<Song> filter(List<Song> songs, SongWordValidationService validationService) {
        List<Song> matched = new ArrayList<>();
        for (Song song : songs) {
            if (validationService.hasBannedOrRejectedWords(song)) {
                matched.add(song);
            }
        }
        return matched;
    }
}
