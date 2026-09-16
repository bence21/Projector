package com.bence.projector.server.backend.service.inreview;

import com.bence.projector.server.backend.model.Song;

import java.util.Date;
import java.util.List;

/**
 * Sorts In-review songs using the same modes as the website song list.
 */
public final class InReviewSongSorter {

    private InReviewSongSorter() {
    }

    public static void sort(List<Song> songs, String sortType) {
        if (songs == null || songs.size() < 2) {
            return;
        }
        if ("TITLE".equals(sortType)) {
            songs.sort(InReviewSongSorter::compareTitle);
        } else if ("MODIFIED_DATE".equals(sortType)) {
            songs.sort(InReviewSongSorter::compareModifiedDateDesc);
        } else if ("VIEWS".equals(sortType)) {
            songs.sort(InReviewSongSorter::compareViewsDesc);
        } else {
            songs.sort(InReviewSongSorter::compareRelevance);
        }
    }

    private static int compareTitle(Song a, Song b) {
        return compareNullable(lower(a.getTitle()), lower(b.getTitle()));
    }

    private static int compareModifiedDateDesc(Song a, Song b) {
        return compareNullable(b.getModifiedDate(), a.getModifiedDate());
    }

    private static int compareViewsDesc(Song a, Song b) {
        return Long.compare(b.getViews(), a.getViews());
    }

    private static int compareRelevance(Song a, Song b) {
        int byScore = Long.compare(b.getScore(), a.getScore());
        if (byScore != 0) {
            return byScore;
        }
        return compareModifiedDateDesc(a, b);
    }

    private static String lower(String title) {
        return title == null ? null : title.toLowerCase();
    }

    @SuppressWarnings("unchecked")
    private static int compareNullable(Comparable left, Comparable right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }

    private static int compareNullable(Date left, Date right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }
}
