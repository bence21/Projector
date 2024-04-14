package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.SongList;

public interface SongListService extends BaseService<SongList> {
    SongList findOneByUuid(String uuid);
}
