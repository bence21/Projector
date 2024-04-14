package projector.repository.ormLite;

import projector.repository.BibleRepository;
import projector.repository.BookRepository;
import projector.repository.CountdownTimeRepository;
import projector.repository.DAOFactory;
import projector.repository.FavouriteSongRepository;
import projector.repository.InformationDAO;
import projector.repository.LanguageRepository;
import projector.repository.LoggedInUserRepository;
import projector.repository.SongBookDAO;
import projector.repository.SongCollectionElementRepository;
import projector.repository.SongCollectionRepository;
import projector.repository.SongDAO;
import projector.repository.SongVerseDAO;
import projector.repository.VerseIndexRepository;
import projector.repository.sqlite.BooksRepository;
import projector.repository.sqlite.BooksRepositoryImpl;
import projector.repository.sqlite.InfoRepository;
import projector.repository.sqlite.InfoRepositoryImpl;
import projector.repository.sqlite.VersesRepository;
import projector.repository.sqlite.VersesRepositoryImpl;

import java.sql.SQLException;

public class OrmLiteDAOFactory extends DAOFactory {

    @Override
    public SongBookDAO getSongBookDAO() {
        try {
            return new SongBookRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SongDAO getSongDAO() {
        try {
            return new SongRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public InformationDAO getInformationDAO() {
        try {
            return new InformationDAOImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SongVerseDAO getSongVerseDAO() {
        try {
            return new SongVerseRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public LanguageRepository getLanguageDAO() {
        try {
            return new LanguageRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SongCollectionRepository getSongCollectionDAO() {
        try {
            return new SongCollectionRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SongCollectionElementRepository getSongCollectionElementDAO() {
        try {
            return new SongCollectionElementRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BibleRepository getBibleDAO() {
        try {
            return new BibleRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BookRepository getBookDAO() {
        try {
            return new BookRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public VerseIndexRepository getVerseIndexDAO() {
        try {
            return new VerseIndexRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BooksRepository getBooksDAO() {
        try {
            return new BooksRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public VersesRepository getVersesDAO() {
        try {
            return new VersesRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public InfoRepository getInfoDAO() {
        try {
            return new InfoRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public CountdownTimeRepository getCountdownTimeDAO() {
        try {
            return new CountdownTimeRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public LoggedInUserRepository getLoggedInUserDAO() {
        try {
            return new LoggedInUserRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public FavouriteSongRepository getFavouriteSongDAO() {
        try {
            return new FavouriteSongRepositoryImpl();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
