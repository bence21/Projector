package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Language;
import org.springframework.data.repository.CrudRepository;

public interface LanguageRepository extends CrudRepository<Language, Long> {

    Language findOneByUuid(String uuid);
}
