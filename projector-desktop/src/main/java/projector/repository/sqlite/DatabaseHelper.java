package projector.repository.sqlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.sqlite.Books;
import projector.model.sqlite.Info;
import projector.model.sqlite.Verses;
import projector.repository.RepositoryException;

import java.sql.SQLException;

public class DatabaseHelper {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);
    private static DatabaseHelper instance;
    private ConnectionSource connectionSource;
    private Dao<Books, Long> booksDao;
    private Dao<Verses, Long> versesDao;
    private Dao<Info, Long> infoDao;

    private DatabaseHelper() {
    }

    public static DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    public void connect(String databasePath) {
        try {
            String databaseUrl = "jdbc:sqlite:" + databasePath;
            connectionSource = new JdbcConnectionSource(databaseUrl);
            System.out.println("Connection successfullyCreated");
        } catch (SQLException e) {
            final String msg = "Unable to create connection";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    public void disconnect() {
        try {
            booksDao = null;
            versesDao = null;
            instance = null;
            connectionSource.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public Dao<Books, Long> getBooksDao() throws SQLException {
        if (booksDao == null) {
            booksDao = DaoManager.createDao(connectionSource, Books.class);
        }
        return booksDao;
    }

    public Dao<Verses, Long> getVersesDao() throws SQLException {
        if (versesDao == null) {
            versesDao = DaoManager.createDao(connectionSource, Verses.class);
        }
        return versesDao;
    }

    public Dao<Info, Long> getInfoDao() throws SQLException {
        if (infoDao == null) {
            infoDao = DaoManager.createDao(connectionSource, Info.class);
        }
        return infoDao;
    }
}
