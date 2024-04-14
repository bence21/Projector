package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.FavouriteSong;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.FavouriteSongRepository;
import com.bence.projector.server.backend.service.FavouriteSongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class FavouriteSongImpl extends BaseServiceImpl<FavouriteSong> implements FavouriteSongService {

    @Autowired
    private FavouriteSongRepository favouriteSongRepository;

    @Override
    public List<FavouriteSong> findAllByUserAndModifiedDateGreaterThan(User user, Date modifiedDate) {
        return favouriteSongRepository.findAllByUserAndModifiedDateGreaterThan(user, modifiedDate);
    }
}
