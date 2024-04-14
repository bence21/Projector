package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.FavouriteSongDTO;
import com.bence.projector.server.backend.model.FavouriteSong;
import com.bence.projector.server.backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FavouriteSongAssembler implements GeneralAssembler<FavouriteSong, FavouriteSongDTO> {

    @Autowired
    private SongService songService;

    @Override
    public FavouriteSongDTO createDto(FavouriteSong favouriteSong) {
        FavouriteSongDTO favouriteSongDTO = new FavouriteSongDTO();
        favouriteSongDTO.setFavourite(favouriteSong.isFavourite());
        favouriteSongDTO.setSongUuid(favouriteSong.getSongUuid());
        favouriteSongDTO.setModifiedDate(favouriteSong.getModifiedDate());
        favouriteSongDTO.setServerModifiedDate(favouriteSong.getServerModifiedDate());
        return favouriteSongDTO;
    }

    @Override
    public FavouriteSong createModel(FavouriteSongDTO favouriteSongDTO) {
        return updateModel(new FavouriteSong(), favouriteSongDTO);
    }

    @Override
    public FavouriteSong updateModel(FavouriteSong favouriteSong, FavouriteSongDTO favouriteSongDTO) {
        if (favouriteSong != null && favouriteSongDTO != null) {
            favouriteSong.setSong(songService.findOneByUuid(favouriteSongDTO.getSongUuid()));
            favouriteSong.setModifiedDate(favouriteSongDTO.getModifiedDate());
            favouriteSong.setFavourite(favouriteSongDTO.isFavourite());
        }
        return favouriteSong;
    }
}
