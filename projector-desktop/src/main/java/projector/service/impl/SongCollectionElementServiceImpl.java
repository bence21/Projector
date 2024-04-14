package projector.service.impl;

import projector.model.Song;
import projector.model.SongCollection;
import projector.model.SongCollectionElement;
import projector.repository.DAOFactory;
import projector.repository.SongCollectionElementRepository;
import projector.service.SongCollectionElementService;

import java.util.List;

public class SongCollectionElementServiceImpl extends AbstractBaseService<SongCollectionElement> implements SongCollectionElementService {

    private final SongCollectionElementRepository songCollectionElementDAO;

    public SongCollectionElementServiceImpl() {
        super(DAOFactory.getInstance().getSongCollectionElementDAO());
        songCollectionElementDAO = DAOFactory.getInstance().getSongCollectionElementDAO();
    }

    @Override
    public List<SongCollectionElement> findBySong(Song song) {
        return songCollectionElementDAO.findBySong(song);
    }

    @Override
    public void findSongsSize(List<SongCollection> songCollections) {
        for (SongCollection songCollection : songCollections) {
            songCollection.setSongsSize(songCollectionElementDAO.countBySongCollection(songCollection));
        }
    }
}
