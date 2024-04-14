package com.bence.songbook.api.assembler;

import android.content.Context;

import com.bence.projector.common.dto.FavouriteSongDTO;
import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

public class FavouriteSongAssembler implements GeneralAssembler<FavouriteSong, FavouriteSongDTO> {

    private final Context context;

    private FavouriteSongAssembler(Context context) {
        this.context = context;
    }

    public static FavouriteSongAssembler getInstance(Context context) {
        return new FavouriteSongAssembler(context);
    }

    @Override
    public FavouriteSong createModel(FavouriteSongDTO favouriteSongDTO) {
        return updateModel(new FavouriteSong(), favouriteSongDTO);
    }

    @Override
    public FavouriteSong updateModel(FavouriteSong favouriteSong, FavouriteSongDTO favouriteSongDTO) {
        if (favouriteSong != null) {
            favouriteSong.setFavourite(favouriteSongDTO.isFavourite());
            favouriteSong.setFavouritePublished(true);
            favouriteSong.setModifiedDate(favouriteSongDTO.getModifiedDate());
            favouriteSong.setUploadedToServer(true);
            SongRepositoryImpl songRepository = new SongRepositoryImpl(context);
            favouriteSong.setSong(songRepository.findByUUID(favouriteSongDTO.getSongUuid()));
        }
        return favouriteSong;
    }

    @Override
    public List<FavouriteSong> createModelList(List<FavouriteSongDTO> favouriteSongDTOS) {
        if (favouriteSongDTOS == null) {
            return null;
        }
        ArrayList<FavouriteSong> favouriteSongs = new ArrayList<>();
        for (FavouriteSongDTO favouriteSongDTO : favouriteSongDTOS) {
            favouriteSongs.add(createModel(favouriteSongDTO));
        }
        return favouriteSongs;
    }

    public FavouriteSongDTO createDto(FavouriteSong favouriteSong) {
        FavouriteSongDTO favouriteSongDTO = new FavouriteSongDTO();
        if (favouriteSong != null) {
            Song song = favouriteSong.getSong();
            if (song == null) {
                return null;
            }
            favouriteSongDTO.setSongUuid(song.getUuid());
            favouriteSongDTO.setModifiedDate(favouriteSong.getModifiedDate());
            favouriteSongDTO.setFavourite(favouriteSong.isFavourite());
        }
        return favouriteSongDTO;
    }

    public List<FavouriteSongDTO> createDTOS(List<FavouriteSong> favouriteSongs) {
        ArrayList<FavouriteSongDTO> favouriteSongDTOS = new ArrayList<>();
        for (FavouriteSong favouriteSong : favouriteSongs) {
            favouriteSongDTOS.add(createDto(favouriteSong));
        }
        return favouriteSongDTOS;
    }
}
