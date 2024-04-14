package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.SongListElement;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

public interface SongListElementRepository extends CrudRepository<SongListElement, Long> {
    @Transactional
    void deleteAllBySongListId(Long id);
}
