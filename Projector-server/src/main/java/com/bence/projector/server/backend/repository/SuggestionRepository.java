package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Suggestion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface SuggestionRepository extends CrudRepository<Suggestion, Long> {
    List<Suggestion> findAllByModifiedDateGreaterThan(Date createdDate);

    Suggestion findOneByUuid(String uuid);

    List<Suggestion> findAllBySongLanguageId(Long languageId);

    @Query("select s from Suggestion s where s.song.uuid = :songUuid")
    List<Suggestion> findAllBySongUuid(@Param("songUuid") String songUuid);
}
