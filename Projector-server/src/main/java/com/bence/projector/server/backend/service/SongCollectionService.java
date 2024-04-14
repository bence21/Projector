package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongCollection;

import java.util.Date;
import java.util.List;

public interface SongCollectionService extends BaseService<SongCollection> {

    List<SongCollection> findAllByLanguageAndAndModifiedDateGreaterThan(Language language, Date lastModifiedDate);

    boolean matches(SongCollection savedSongCollection, SongCollection songCollection1);

    List<SongCollection> findAllBySong(Song song);

    SongCollection saveWithoutForeign(SongCollection songCollection);

    SongCollection findOneByUuid(String uuid);

    List<SongCollection> findAllByLanguage(Language language);
}
