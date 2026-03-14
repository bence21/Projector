package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Bible;
import com.bence.projector.server.backend.model.Language;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BibleRepository extends CrudRepository<Bible, Long> {
    Bible findOneByUuid(String uuid);

    @EntityGraph(attributePaths = {"books", "books.chapters", "books.chapters.verses"})
    List<Bible> findAllByLanguage(Language language);
}
