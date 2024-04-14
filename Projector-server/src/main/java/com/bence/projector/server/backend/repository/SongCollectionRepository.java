package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.SongCollection;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface SongCollectionRepository extends CrudRepository<SongCollection, Long> {

    List<SongCollection> findAllByLanguage_IdAndAndModifiedDateGreaterThan(Long language_id, Date modifiedDate);

    List<SongCollection> findAllBySongCollectionElements_SongId(Long songId);

    SongCollection findOneByUuid(String uuid);

    List<SongCollection> findAllByLanguage_Id(Long language_id);
}
