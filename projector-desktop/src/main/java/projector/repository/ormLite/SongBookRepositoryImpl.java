package projector.repository.ormLite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Song;
import projector.model.SongBook;
import projector.model.SongBookSong;
import projector.repository.RepositoryException;
import projector.repository.SongBookDAO;

import java.sql.SQLException;
import java.util.List;

public class SongBookRepositoryImpl extends AbstractBaseRepository<SongBook> implements SongBookDAO {

    private static final Logger LOG = LoggerFactory.getLogger(SongBookRepositoryImpl.class);
    private Dao<SongBookSong, Long> songBookSongDao;
    private Dao<Song, Long> songDao;
    private PreparedQuery<Song> songsForSongBookQuery;

    SongBookRepositoryImpl() throws SQLException {
        super(SongBook.class, DatabaseHelper.getInstance().getSongBookDao());
        songBookSongDao = DatabaseHelper.getInstance().getSongBookSongDao();
    }

    @Override
    public SongBook create(SongBook model) throws RepositoryException {
        final SongBook songBook = super.create(model);
        try {
            for (Song song : model.getSongs()) {
                final SongBookSong songBookSong = new SongBookSong(model, song);
                songBookSongDao.create(songBookSong);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return songBook;
    }

    @Override
    public List<SongBook> create(List<SongBook> songBooks) throws RepositoryException {
        for (SongBook songBook : songBooks) {
            create(songBook);
        }
        return songBooks;
    }

    @Override
    public List<SongBook> findAll() {
        final List<SongBook> songBooks = super.findAll();
        try {
            for (SongBook songBook : songBooks) {
                songBook.setSongs(lookupPostsForUser(songBook));
            }
        } catch (SQLException e) {
            final String msg = "Could not set songs for songbooks";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
        return songBooks;
    }

    private List<Song> lookupPostsForUser(SongBook songBook) throws SQLException {
        if (songsForSongBookQuery == null) {
            songsForSongBookQuery = makeSongsForSongBookQuery();
        }
        songsForSongBookQuery.setArgumentHolderValue(0, songBook);
        return songDao.query(songsForSongBookQuery);
    }

    private PreparedQuery<Song> makeSongsForSongBookQuery() throws SQLException {
        QueryBuilder<SongBookSong, Long> songBookSongQb = songBookSongDao.queryBuilder();
        songBookSongQb.selectColumns("song_id");
        SelectArg songBookSelectArg = new SelectArg();
        songBookSongQb.where().eq("songBook_id", songBookSelectArg);
        if (songDao == null) {
            songDao = DatabaseHelper.getInstance().getSongDao();
        }
        QueryBuilder<Song, Long> songQb = songDao.queryBuilder();
        songQb.where().in("id", songBookSongQb);
        return songQb.prepare();
    }
}
