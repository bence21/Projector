package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Song;

/**
 * Which songs to include by {@link Song#isPublic()} for batch work (e.g. mark-similar).
 * SQL predicate: {@link com.bence.projector.server.backend.service.impl.SongServiceImpl}.
 */
public enum SongPublicScope {
    /**
     * Rows where {@link Song#isPublic()} would be {@code true}.
     */
    PUBLIC,
    /**
     * Rows where {@link Song#isPublic()} would be {@code false}.
     */
    NON_PUBLIC;

    public boolean matches(Song song) {
        if (this == PUBLIC) {
            return song.isPublic();
        }
        return !song.isPublic();
    }

    public static SongPublicScope fromRequestParam(String s) {
        if (s == null || s.isBlank()) {
            return PUBLIC;
        }
        String t = s.trim();
        if ("public".equalsIgnoreCase(t)) {
            return PUBLIC;
        }
        if ("nonPublic".equalsIgnoreCase(t) || "non_public".equalsIgnoreCase(t)) {
            return NON_PUBLIC;
        }
        throw new IllegalArgumentException("visibility must be 'public' or 'nonPublic'");
    }
}
