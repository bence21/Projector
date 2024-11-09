package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongLink;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SongLinkRepository extends CrudRepository<SongLink, Long> {
    SongLink findOneByUuid(String uuid);

    List<SongLink> findAllBySong1OrSong2(Song song1, Song song2);
}
