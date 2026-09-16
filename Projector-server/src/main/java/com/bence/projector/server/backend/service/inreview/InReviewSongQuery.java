package com.bence.projector.server.backend.service.inreview;

import com.bence.projector.server.backend.model.Language;

/**
 * Query for a paged / exported In-review title list.
 */
public class InReviewSongQuery {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    private final Language language;
    private final String createdByEmail;
    private final Integer maxLyricChars;
    private final boolean bannedOrRejected;
    private final String titleSearch;
    private final String sortType;
    private final int page;
    private final int size;

    public InReviewSongQuery(Language language,
                             String createdByEmail,
                             Integer maxLyricChars,
                             boolean bannedOrRejected,
                             String titleSearch,
                             String sortType,
                             int page,
                             int size) {
        this.language = language;
        this.createdByEmail = createdByEmail;
        this.maxLyricChars = maxLyricChars;
        this.bannedOrRejected = bannedOrRejected;
        this.titleSearch = titleSearch;
        this.sortType = sortType;
        this.page = Math.max(page, 0);
        this.size = clampSize(size);
    }

    private static int clampSize(int size) {
        if (size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    public Language getLanguage() {
        return language;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public Integer getMaxLyricChars() {
        return maxLyricChars;
    }

    public boolean isBannedOrRejected() {
        return bannedOrRejected;
    }

    public String getTitleSearch() {
        return titleSearch;
    }

    public String getSortType() {
        return sortType;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public boolean needsVerses() {
        return maxLyricChars != null || bannedOrRejected;
    }
}
