package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.BibleVerse;
import org.springframework.data.repository.CrudRepository;

public interface BibleVerseRepository extends CrudRepository<BibleVerse, Long> {
}
