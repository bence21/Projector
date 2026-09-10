package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerseOrderListItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;

public interface SongVerseOrderListItemRepository extends CrudRepository<SongVerseOrderListItem, Long> {
    /**
     * Bulk delete so delete-and-recreate in {@code SongServiceImpl#save} does not schedule
     * entity-level deletes that can fail with {@code StaleStateException} on flush.
     */
    @Modifying(flushAutomatically = true)
    @Query("delete from SongVerseOrderListItem item where item.song = :song")
    @Transactional
    void deleteBySong(@Param("song") Song song);
}
