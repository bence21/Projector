package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Chapter;
import org.springframework.data.repository.CrudRepository;

public interface ChapterRepository extends CrudRepository<Chapter, Long> {
}
