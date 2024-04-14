package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Bible;

public interface BibleService extends BaseService<Bible> {

    Bible findOneByUuid(String uuid);

    void saveToBooks(Bible bible);
}
