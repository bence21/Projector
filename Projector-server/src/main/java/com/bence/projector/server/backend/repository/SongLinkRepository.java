package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongLink;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongLinkRepository extends CrudRepository<SongLink, Long> {
    SongLink findOneByUuid(String uuid);

    List<SongLink> findAllBySong1OrSong2(Song song1, Song song2);

    @Query("""
                SELECT sl
                FROM SongLink sl
                WHERE sl.applied = false AND
                      (sl.song1.language = :language OR sl.song2.language = :language)
            """)
    List<SongLink> findAllUnAppliedByLanguage(@Param("language") Language language);

    @Query("""
                SELECT sl
                FROM SongLink sl
                WHERE sl.applied = false
            """)
    List<SongLink> findAllUnApplied();
}
