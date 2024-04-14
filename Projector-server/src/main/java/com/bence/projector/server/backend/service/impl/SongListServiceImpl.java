package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.SongList;
import com.bence.projector.server.backend.model.SongListElement;
import com.bence.projector.server.backend.repository.SongListElementRepository;
import com.bence.projector.server.backend.repository.SongListRepository;
import com.bence.projector.server.backend.service.SongListElementService;
import com.bence.projector.server.backend.service.SongListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SongListServiceImpl extends BaseServiceImpl<SongList> implements SongListService {

    @Autowired
    private SongListRepository songListRepository;
    @Autowired
    private SongListElementRepository songListElementRepository;
    @Autowired
    private SongListElementService songListElementService;

    @Override
    public SongList findOneByUuid(String uuid) {
        return songListRepository.findOneByUuid(uuid);
    }

    @Override
    public SongList save(SongList songList) {
        List<SongListElement> songListElements = new ArrayList<>(songList.getSongListElements());
        Long id = songList.getId();
        if (id != null) {
            songListElementRepository.deleteAllBySongListId(id);
        }
        SongList saved = songListRepository.save(songList);
        songListElementService.saveAllByRepository(songListElements);
        return saved;
    }

    @Override
    public Iterable<SongList> save(List<SongList> songLists) {
        for (SongList songList : songLists) {
            save(songList);
        }
        return songLists;
    }
}
