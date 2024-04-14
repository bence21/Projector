package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Bible;
import org.springframework.data.repository.CrudRepository;

public interface BibleRepository extends CrudRepository<Bible, Long> {
    Bible findOneByUuid(String uuid);
}
