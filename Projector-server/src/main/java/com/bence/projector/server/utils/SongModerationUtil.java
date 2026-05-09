package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Song;

public final class SongModerationUtil {

    private SongModerationUtil() {
    }

    /**
     * Routes a song to review queue (non-public state).
     */
    public static void markSongForReviewQueue(Song song) {
        song.setDeleted(true);
        song.setUploaded(true);
    }
}
