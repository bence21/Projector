package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Suggestion;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface SuggestionRepository extends CrudRepository<Suggestion, Long> {
    List<Suggestion> findAllByModifiedDateGreaterThan(Date createdDate);

    Suggestion findOneByUuid(String uuid);

    List<Suggestion> findAllBySongLanguageId(Long languageId);
}
