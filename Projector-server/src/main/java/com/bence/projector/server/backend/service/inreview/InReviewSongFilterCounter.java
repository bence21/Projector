package com.bence.projector.server.backend.service.inreview;

import com.bence.projector.common.dto.InReviewSongFilterCountsDTO;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.service.SongWordValidationService;
import com.bence.projector.server.utils.SongLyricCharCounter;

import java.util.List;

/**
 * One scan of In-review songs to produce independent short-lyric and banned-word counts.
 */
public final class InReviewSongFilterCounter {

    private InReviewSongFilterCounter() {
    }

    public static InReviewSongFilterCountsDTO count(List<Song> songs, SongWordValidationService validationService) {
        long shortLyrics = 0;
        long bannedOrRejected = 0;
        for (Song song : songs) {
            if (SongLyricCharCounter.isShortLyrics(song, SongLyricCharCounter.SHORT_LYRIC_MAX_CHARS)) {
                shortLyrics++;
            }
            if (validationService.hasBannedOrRejectedWords(song)) {
                bannedOrRejected++;
            }
        }
        return new InReviewSongFilterCountsDTO(songs.size(), shortLyrics, bannedOrRejected);
    }
}
