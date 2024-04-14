package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.FavouriteSong;
import com.bence.projector.server.backend.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface FavouriteSongRepository extends CrudRepository<FavouriteSong, Long> {
    List<FavouriteSong> findAllByUserAndModifiedDateGreaterThan(User user, Date modifiedDate);
}
