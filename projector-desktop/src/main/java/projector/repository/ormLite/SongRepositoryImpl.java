package projector.repository.ormLite;

import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import com.j256.ormlite.misc.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Song;
import projector.repository.RepositoryException;
import projector.repository.SongDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

public class SongRepositoryImpl extends AbstractBaseRepository<Song> implements SongDAO {
    private static final Logger LOG = LoggerFactory.getLogger(SongRepositoryImpl.class);
    private final DatabaseHelper databaseHelper;

    private SongVerseRepositoryImpl songVerseRepository;

    SongRepositoryImpl() throws SQLException {
        super(Song.class, DatabaseHelper.getInstance().getSongDao());
        databaseHelper = DatabaseHelper.getInstance();
        songVerseRepository = new SongVerseRepositoryImpl();
    }

    @Override
    public Song create(Song song) throws RepositoryException {
        Long id = song.getId();
        if (id != null) {
            Song byId = findById(id);
            if (byId != null) {
                songVerseRepository.deleteAll(byId.getVerses());
            }
        }
        final Song song1 = super.create(song);
        songVerseRepository.create(song.getVerses());
        return song1;
    }

    @Override
    public List<Song> create(List<Song> songs) throws RepositoryException {
        List<Song> songList = super.create(songs);
        for (Song song : songList) {
            songVerseRepository.create(song.getVerses());
        }
        return songList;
    }

    @Override
    public boolean delete(Song song) throws RepositoryException {
        songVerseRepository.deleteAll(song.getVerses());
        return super.delete(song);
    }

    @Override
    public boolean deleteAll(List<Song> songs) throws RepositoryException {
        for (Song song : songs) {
            delete(song);
        }
        return true;
    }

    @Override
    public Song findByTitle(String title) {
        String msg = "Could not find song";
        try {
            List<Song> songs = dao.queryForEq("title", title);
            if (songs != null && songs.size() > 0) {
                return songs.get(0);
            }
            return null;
        } catch (Exception e) {
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<Song> findAllByVersionGroup(String versionGroup) {
        String msg = "Could not find song versions";
        try {
            List<Song> songs = dao.queryForEq("versionGroup", versionGroup);
            Song byUUID = findByUuid(versionGroup);
            if (byUUID != null) {
                songs.add(byUUID);
            }
            return songs;
        } catch (Exception e) {
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void saveViews(List<SongViewsDTO> songViewsDTOS) {
        try {
            TransactionManager.callInTransaction(databaseHelper.getConnectionSource(),
                    (Callable<Void>) () -> {
                        if (songViewsDTOS.size() > 0) {
                            for (SongViewsDTO songViewsDTO : songViewsDTOS) {
                                dao.executeRaw("UPDATE song SET views = "
                                        + songViewsDTO.getViews()
                                        + " WHERE uuid = '" + songViewsDTO.getUuid() + "'");
                            }
                        }
                        return null;
                    });
        } catch (SQLException e) {
            String msg = "Could not save views";
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void saveFavouriteCount(List<SongFavouritesDTO> songFavouritesDTOS) {
        try {
            TransactionManager.callInTransaction(databaseHelper.getConnectionSource(),
                    (Callable<Void>) () -> {
                        if (songFavouritesDTOS.size() > 0) {
                            for (SongFavouritesDTO songFavouritesDTO : songFavouritesDTOS) {
                                dao.executeRaw("UPDATE song SET favouriteCount = "
                                        + songFavouritesDTO.getFavourites()
                                        + " WHERE uuid = '" + songFavouritesDTO.getUuid() + "'");
                            }
                        }
                        return null;
                    });
        } catch (SQLException e) {
            String msg = "Could not save favouriteCount";
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }
}
