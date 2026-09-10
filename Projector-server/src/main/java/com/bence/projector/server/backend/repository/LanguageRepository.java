package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Language;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LanguageRepository extends CrudRepository<Language, Long> {

    Language findOneByUuid(String uuid);

    @Query("SELECT l FROM Language l WHERE LOWER(TRIM(l.englishName)) = LOWER(TRIM(:englishName)) " +
            "AND (l.deleted IS NULL OR l.deleted = FALSE)")
    List<Language> findActiveByEnglishName(@Param("englishName") String englishName);
}
