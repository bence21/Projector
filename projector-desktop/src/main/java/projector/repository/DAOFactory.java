package projector.repository;

import projector.repository.ormLite.OrmLiteDAOFactory;
import projector.repository.sqlite.BooksRepository;
import projector.repository.sqlite.InfoRepository;
import projector.repository.sqlite.VersesRepository;

public abstract class DAOFactory {

    public static DAOFactory getInstance() {
        return new OrmLiteDAOFactory();
    }

    public abstract SongBookDAO getSongBookDAO();

    public abstract SongDAO getSongDAO();

    public abstract InformationDAO getInformationDAO();

    public abstract SongVerseDAO getSongVerseDAO();

    public abstract LanguageRepository getLanguageDAO();

    public abstract SongCollectionRepository getSongCollectionDAO();

    public abstract SongCollectionElementRepository getSongCollectionElementDAO();

    public abstract BibleRepository getBibleDAO();

    public abstract BookRepository getBookDAO();

    public abstract VerseIndexRepository getVerseIndexDAO();

    public abstract BooksRepository getBooksDAO();

    public abstract VersesRepository getVersesDAO();

    public abstract InfoRepository getInfoDAO();

    public abstract CountdownTimeRepository getCountdownTimeDAO();

    public abstract LoggedInUserRepository getLoggedInUserDAO();

    public abstract FavouriteSongRepository getFavouriteSongDAO();
}
