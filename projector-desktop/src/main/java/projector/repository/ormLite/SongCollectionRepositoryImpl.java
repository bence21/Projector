package projector.repository.ormLite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Language;
import projector.model.SongCollection;
import projector.repository.RepositoryException;
import projector.repository.SongCollectionElementRepository;
import projector.repository.SongCollectionRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SongCollectionRepositoryImpl extends AbstractBaseRepository<SongCollection> implements SongCollectionRepository {
    private static final Logger LOG = LoggerFactory.getLogger(SongCollectionRepositoryImpl.class);
    private SongCollectionElementRepository songCollectionElementRepository;

    SongCollectionRepositoryImpl() throws SQLException {
        super(SongCollection.class, DatabaseHelper.getInstance().getSongCollectionDao());
        try {
            songCollectionElementRepository = new SongCollectionElementRepositoryImpl();
        } catch (SQLException e) {
            String msg = "Failed to initialize SongCollectionRepository";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<SongCollection> findAllByLanguage(Language language) {
        List<SongCollection> allSongCollections = findAll();
        List<SongCollection> songCollections = new ArrayList<>();
        Long uuid = language.getId();
        for (SongCollection songCollection : allSongCollections) {
            try {
                if (songCollection.getLanguage().getId().equals(uuid)) {
                    songCollections.add(songCollection);
                }
            } catch (NullPointerException e) {
                LOG.error(e.getMessage(), e);
                LOG.debug(songCollection.getName());
            }
        }
        return songCollections;
    }

    @Override
    public SongCollection create(SongCollection songCollection) {
        SongCollection collection = super.create(songCollection);
        songCollectionElementRepository.create(songCollection.getSongCollectionElements());
        return collection;
    }

    @Override
    public List<SongCollection> create(List<SongCollection> songCollections) {
        for (SongCollection songCollection : songCollections) {
            create(songCollection);
        }
        return songCollections;
    }
}
