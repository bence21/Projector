package projector.service.impl;

import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import com.j256.ormlite.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Language;
import projector.model.Song;
import projector.repository.DAOFactory;
import projector.repository.RepositoryException;
import projector.repository.SongDAO;
import projector.repository.ormLite.DatabaseHelper;
import projector.service.ServiceException;
import projector.service.SongService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static projector.repository.ormLite.VerseIndexRepositoryImpl.countByField;

public class SongServiceImpl extends AbstractBaseService<Song> implements SongService {

    private final static Logger LOG = LoggerFactory.getLogger(SongServiceImpl.class);
    private final SongDAO songDAO = DAOFactory.getInstance().getSongDAO();
    private final Dao<Song, Long> dao;
    private final HashMap<Long, Song> hashMap = new HashMap<>();
    @SuppressWarnings("FieldCanBeLocal")
    private final String TABLE_NAME = "SONG";

    public SongServiceImpl() throws SQLException {
        super(DAOFactory.getInstance().getSongDAO());
        dao = DatabaseHelper.getInstance().getSongDao();
    }

    @Override
    public Song findByUuid(String uuid) {
        Song song = super.findByUuid(uuid);
        return getFromMemoryOrSong(song);
    }

    private Song getSongFromHashMap(Song song, boolean updateMap) {
        if (song == null) {
            return null;
        }
        Long id = song.getId();
        if (hashMap.containsKey(id)) {
            return hashMap.get(id);
        } else if (updateMap) {
            hashMap.put(id, song);
        }
        return song;
    }

    @Override
    public Song findById(Long id) {
        Song song = super.findById(id);
        return getFromMemoryOrSong(song);
    }

    @Override
    public List<Song> findAll() throws ServiceException {
        try {
            List<Song> songList = songDAO.findAll();
            return getSongsFromHashMap(songList);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    private List<Song> getSongsFromHashMap(List<Song> songList) {
        List<Song> songs = new ArrayList<>(songList.size());
        for (Song song : songList) {
            songs.add(getFromMemoryOrSong(song));
        }
        return songs;
    }

    @Override
    public Song create(Song song) throws ServiceException {
        return updateSong(song);
    }

    private Song updateSong(Song song) {
        try {
            Song updatedSong = songDAO.create(song);
            if (updatedSong != null) {
                hashMap.put(updatedSong.getId(), updatedSong);
            }
            return updatedSong;
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Song update(Song song) throws ServiceException {
        return updateSong(song);
    }

    @Override
    public boolean delete(Song song) throws ServiceException {
        try {
            return songDAO.delete(song);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(List<Song> songs) throws ServiceException {
        try {
            return songDAO.deleteAll(songs);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Song findByTitle(String title) {
        try {
            Song song = songDAO.findByTitle(title);
            return getFromMemoryOrSong(song);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Song> findAllByVersionGroup(String versionGroup) {
        try {
            List<Song> songs = songDAO.findAllByVersionGroup(versionGroup);
            return getSongsFromHashMap(songs);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void saveViews(List<SongViewsDTO> songViewsDTOS) {
        try {
            songDAO.saveViews(songViewsDTOS);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void saveFavouriteCount(List<SongFavouritesDTO> songFavouritesDTOS) {
        try {
            songDAO.saveFavouriteCount(songFavouritesDTOS);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Song getFromMemoryOrSong(Song song) {
        return getSongFromHashMap(song, true);
    }

    @Override
    public Song getFromMemoryOrSongNoUpdate(Song song) {
        return getSongFromHashMap(song, false);
    }

    @Override
    public long countByLanguage(Language language) {
        if (language == null) {
            return 0;
        }
        return countByField(TABLE_NAME, "LANGUAGE_ID", language.getId(), dao);
    }
}
