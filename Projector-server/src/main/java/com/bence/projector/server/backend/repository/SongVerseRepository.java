package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.SongVerse;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

public interface SongVerseRepository extends CrudRepository<SongVerse, Long> {
    @Transactional
    void deleteAllBySongId(Long songId);

    List<SongVerse> findAllBySong_LanguageAndSong_ModifiedDateGreaterThan(Language language, Date modifiedDate);

    @Transactional
    void deleteAllBySuggestionId(Long suggestionId);
}
