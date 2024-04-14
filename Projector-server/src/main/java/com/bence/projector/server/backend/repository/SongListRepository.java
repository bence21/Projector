package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.SongList;
import org.springframework.data.repository.CrudRepository;

public interface SongListRepository extends CrudRepository<SongList, Long> {
    SongList findOneByUuid(String uuid);
}
