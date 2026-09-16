package com.bence.projector.server.backend.service.inreview;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Accent-insensitive title contains-search for In-review songs.
 */
public final class InReviewSongTitleSearch {

    private InReviewSongTitleSearch() {
    }

    public static List<Song> filter(List<Song> songs, String titleSearch) {
        if (songs == null || songs.isEmpty()) {
            return songs == null ? List.of() : songs;
        }
        if (titleSearch == null || titleSearch.isBlank()) {
            return songs;
        }
        String needle = normalize(titleSearch);
        if (needle.isEmpty()) {
            return songs;
        }
        List<Song> matched = new ArrayList<>();
        for (Song song : songs) {
            String title = song.getTitle();
            if (title != null && normalize(title).contains(needle)) {
                matched.add(song);
            }
        }
        return matched;
    }

    private static String normalize(String value) {
        return StringUtils.stripAccents(value).toLowerCase();
    }
}
