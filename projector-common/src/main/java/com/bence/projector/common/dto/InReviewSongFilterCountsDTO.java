package com.bence.projector.common.dto;

/**
 * Independent match counts for English (or any language) In-review queue filters.
 */
public class InReviewSongFilterCountsDTO {

    private long total;
    private long shortLyrics;
    private long bannedOrRejected;

    public InReviewSongFilterCountsDTO() {
    }

    public InReviewSongFilterCountsDTO(long total, long shortLyrics, long bannedOrRejected) {
        this.total = total;
        this.shortLyrics = shortLyrics;
        this.bannedOrRejected = bannedOrRejected;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getShortLyrics() {
        return shortLyrics;
    }

    public void setShortLyrics(long shortLyrics) {
        this.shortLyrics = shortLyrics;
    }

    public long getBannedOrRejected() {
        return bannedOrRejected;
    }

    public void setBannedOrRejected(long bannedOrRejected) {
        this.bannedOrRejected = bannedOrRejected;
    }
}
