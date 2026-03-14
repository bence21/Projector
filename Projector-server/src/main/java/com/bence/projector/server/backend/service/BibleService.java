package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Bible;
import com.bence.projector.server.backend.model.Language;

import java.util.List;

public interface BibleService extends BaseService<Bible> {

    Bible findOneByUuid(String uuid);

    List<Bible> findAllByLanguage(Language language);

    void saveToBooks(Bible bible);
}
