package com.bence.projector.server.backend.service.inreview;

import java.util.Collections;
import java.util.List;

/**
 * Slices an already-filtered In-review list into a 0-based page.
 */
public final class InReviewSongPaginator {

    private InReviewSongPaginator() {
    }

    public static <T> List<T> page(List<T> items, int page, int size) {
        if (items == null || items.isEmpty() || size < 1) {
            return List.of();
        }
        int start = Math.max(page, 0) * size;
        if (start >= items.size()) {
            return List.of();
        }
        int end = Math.min(start + size, items.size());
        return Collections.unmodifiableList(items.subList(start, end));
    }
}
