package com.bence.songbook.repository;

import com.bence.songbook.models.SongListElement;

public interface SongListElementRepository extends BaseRepository<SongListElement> {

    /**
     * Persists a swap of the {@code number} field between two elements inside a single
     * transaction, using a placeholder value to avoid unique constraint violations.
     */
    void saveSwap(SongListElement first, SongListElement second, int firstNumber, int secondNumber);
}
