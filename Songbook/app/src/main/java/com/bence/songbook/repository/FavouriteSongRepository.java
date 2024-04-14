package com.bence.songbook.repository;

import com.bence.songbook.models.FavouriteSong;

public interface FavouriteSongRepository extends BaseRepository<FavouriteSong> {

    FavouriteSong findFavouriteSongBySongUuid(String uuid);
}
