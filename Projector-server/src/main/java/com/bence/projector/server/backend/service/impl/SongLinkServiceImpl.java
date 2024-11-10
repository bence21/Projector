package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.SongLink;
import com.bence.projector.server.backend.repository.SongLinkRepository;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.SongLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SongLinkServiceImpl extends BaseServiceImpl<SongLink> implements SongLinkService {

    @Autowired
    private SongLinkRepository songLinkRepository;
    @Autowired
    private SongRepository songRepository;

    @Override
    public List<SongLink> findAllByLanguage(Language language) {
        return songLinkRepository.findAllUnAppliedByLanguage(language);
    }

    private List<SongLink> getUnAppliedSongLinks() {
        return songLinkRepository.findAllUnApplied();
    }

    @Override
    public List<SongLink> resolveAppliedSongLinks() {
        List<SongLink> songLinks = getUnAppliedSongLinks();
        List<SongLink> appliedSongLinks = new ArrayList<>();
        for (SongLink songLink : songLinks) {
            if (songLink.isUnApplied() && songLink.alreadyTheSameVersionGroup(songRepository)) {
                songLink.setApplied(true);
                songLink.setModifiedDate(new Date());
                appliedSongLinks.add(songLink);
            }
        }
        saveAllByRepository(appliedSongLinks);
        return appliedSongLinks;
    }

    @Override
    public List<SongLink> findAllUnApplied() {
        return getUnAppliedSongLinks();
    }

    @Override
    public SongLink findOneByUuid(String uuid) {
        return songLinkRepository.findOneByUuid(uuid);
    }
}
