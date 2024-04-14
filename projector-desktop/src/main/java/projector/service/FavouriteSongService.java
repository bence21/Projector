package projector.service;

import projector.model.FavouriteSong;
import projector.service.impl.FavouriteSongServiceImpl;

public interface FavouriteSongService extends CrudService<FavouriteSong> {
    void syncFavourites();

    void syncFavouritesFromServer(FavouriteSongServiceImpl.FavouriteSongUpdateListener favouriteSongUpdateListener);
}
