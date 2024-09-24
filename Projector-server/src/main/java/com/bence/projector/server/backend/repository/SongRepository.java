package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

public interface SongRepository extends CrudRepository<Song, Long> {
    List<Song> findAllByModifiedDateGreaterThan(Date modifiedDate);

    List<Song> findAllByVersionGroup(Song versionGroup);

    List<Song> findAllByYoutubeUrlNotNull();

    Song findOneByUuid(String uuid);

    long countByLanguage(Language language);

    List<Song> findAllByLanguage(Language language, Pageable pageable);

    long countAllByLanguageAndIsBackUpIsNullAndDeletedIsFalse(Language language);

    @Transactional
    List<Song> findAllByModifiedDateGreaterThanAndLanguage(Date lastModifiedDate, Language language);

    List<Song> findAllByLanguageAndUploadedIsTrueAndIsBackUpIsNullAndDeletedIsTrue(Language language);

    List<Song> findAllByCreatedByEmail(String createdByEmail);

    List<Song> findAllByLanguageAndCreatedByEmail(Language language, String createdByEmail);

    List<Song> findAllByVersesIsEmpty();

    Song findByBackUp(Song song);

    @Query(value = "SELECT DISTINCT s.* FROM song s " +
            "JOIN song_verse v ON s.id = v.song_id " +
            "WHERE s.language_id = :language_id" +
            " AND (s.is_back_up <> 1 or s.is_back_up is null)" +
            " AND (s.reviewer_erased <> 1 or s.reviewer_erased is null)" +
            " AND (s.deleted <> 1 or s.deleted is null)" +
            " AND v.text REGEXP :text",
            nativeQuery = true)
    List<Song> findAllByLanguageAndVersesTextContains(
            @Param("language_id") Long languageId,
            @Param("text") String text);
}
