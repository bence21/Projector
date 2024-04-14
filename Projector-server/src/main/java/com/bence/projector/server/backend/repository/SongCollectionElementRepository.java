package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.SongCollectionElement;
import org.springframework.data.repository.CrudRepository;

public interface SongCollectionElementRepository extends CrudRepository<SongCollectionElement, Long> {
}
