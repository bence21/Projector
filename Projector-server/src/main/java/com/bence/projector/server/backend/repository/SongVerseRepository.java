package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.SongVerse;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

public interface SongVerseRepository extends CrudRepository<SongVerse, Long> {
    /**
     * Bulk delete (not SELECT + {@code EntityManager#remove}). Derived {@code deleteAllBy...}
     * schedules entity deletes that can collide with delete-and-recreate in the same transaction
     * ({@code StaleStateException} / unexpected row count on {@code delete from song_verse}).
     */
    @Modifying(flushAutomatically = true)
    @Query("delete from SongVerse sv where sv.song.id = :songId")
    @Transactional
    void deleteAllBySongId(@Param("songId") Long songId);

    @Modifying(flushAutomatically = true)
    @Query("delete from SongVerse sv where sv.suggestion.id = :suggestionId")
    @Transactional
    void deleteAllBySuggestionId(@Param("suggestionId") Long suggestionId);
}
