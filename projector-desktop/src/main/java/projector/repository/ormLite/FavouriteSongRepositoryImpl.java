package projector.repository.ormLite;

import projector.model.FavouriteSong;
import projector.repository.FavouriteSongRepository;

import java.sql.SQLException;

public class FavouriteSongRepositoryImpl extends AbstractBaseRepository<FavouriteSong> implements FavouriteSongRepository {

    FavouriteSongRepositoryImpl() throws SQLException {
        super(FavouriteSong.class, DatabaseHelper.getInstance().getFavouriteSongDao().getDao());
    }
}
