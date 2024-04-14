package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.SongLink;
import org.springframework.data.repository.CrudRepository;

public interface SongLinkRepository extends CrudRepository<SongLink, Long> {
    SongLink findOneByUuid(String uuid);
}
