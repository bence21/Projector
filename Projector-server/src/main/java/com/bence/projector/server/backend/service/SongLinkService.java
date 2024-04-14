package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.SongLink;

import java.util.List;

public interface SongLinkService extends BaseService<SongLink> {
    List<SongLink> findAllByLanguage(Language language);

    List<SongLink> resolveAppliedSongLinks();

    List<SongLink> findAllUnApplied();

    SongLink findOneByUuid(String uuid);
}
