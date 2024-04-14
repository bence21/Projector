package com.bence.songbook.api.assembler;

import android.content.Context;

import com.bence.projector.common.dto.SongListDTO;
import com.bence.projector.common.dto.SongListElementDTO;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongList;
import com.bence.songbook.models.SongListElement;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class SongListAssembler implements GeneralAssembler<SongList, SongListDTO> {

    private final Context context;

    private SongListAssembler(Context context) {
        this.context = context;
    }

    public static SongListAssembler getInstance(Context context) {
        return new SongListAssembler(context);
    }

    @Override
    public synchronized SongList createModel(SongListDTO songListDTO) {
        return updateModel(new SongList(), songListDTO);
    }

    @Override
    public synchronized SongList updateModel(SongList songList, SongListDTO songListDTO) {
        if (songList != null) {
            songList.setTitle(songListDTO.getTitle());
            songList.setDescription(songListDTO.getDescription());
            songList.setCreatedDate(songListDTO.getCreatedDate());
            songList.setModifiedDate(songListDTO.getModifiedDate());
            ArrayList<SongListElement> songListElements = new ArrayList<>();
            SongRepositoryImpl songRepository = new SongRepositoryImpl(context);
            for (SongListElementDTO dto : songListDTO.getSongListElements()) {
                Song byUUID = songRepository.findByUUID(dto.getSongUuid());
                if (byUUID != null) {
                    SongListElement listElement = new SongListElement();
                    listElement.setNumber(dto.getNumber());
                    listElement.setSong(byUUID);
                    songListElements.add(listElement);
                }
            }
            songList.setSongListElements(songListElements);
        }
        return songList;
    }

    @Override
    public synchronized List<SongList> createModelList(List<SongListDTO> ds) {
        if (ds == null) {
            return null;
        }
        List<SongList> models = new ArrayList<>();
        for (SongListDTO songListDTO : ds) {
            models.add(createModel(songListDTO));
        }
        return models;
    }

    public SongListDTO createDto(SongList songList) {
        SongListDTO songListDTO = new SongListDTO();
        songListDTO.setUuid(songList.getUuid());
        songListDTO.setTitle(songList.getTitle());
        songListDTO.setDescription(songList.getDescription());
        songListDTO.setCreatedDate(songList.getCreatedDate());
        songListDTO.setModifiedDate(songList.getModifiedDate());
        List<SongListElementDTO> songListElements = new ArrayList<>();
        for (SongListElement element : songList.getSongListElements()) {
            SongListElementDTO songListElementDTO = new SongListElementDTO();
            songListElementDTO.setNumber(element.getNumber());
            Song song = element.getSong();
            if (song == null) {
                continue;
            }
            songListElementDTO.setSongUuid(song.getUuid());
            songListElements.add(songListElementDTO);
        }
        songListDTO.setSongListElements(songListElements);
        return songListDTO;
    }
}
