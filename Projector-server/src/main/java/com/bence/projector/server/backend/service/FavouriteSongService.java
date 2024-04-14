package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.FavouriteSong;
import com.bence.projector.server.backend.model.User;

import java.util.Date;
import java.util.List;

public interface FavouriteSongService extends BaseService<FavouriteSong> {
    List<FavouriteSong> findAllByUserAndModifiedDateGreaterThan(User user, Date modifiedDate);
}
