package projector.service.impl;

import projector.model.Language;
import projector.model.SongCollection;
import projector.repository.DAOFactory;
import projector.repository.SongCollectionRepository;
import projector.service.SongCollectionService;

import java.util.List;

public class SongCollectionServiceImpl extends AbstractBaseService<SongCollection> implements SongCollectionService {

    private SongCollectionRepository songCollectionDAO;

    public SongCollectionServiceImpl() {
        super(DAOFactory.getInstance().getSongCollectionDAO());
        songCollectionDAO = DAOFactory.getInstance().getSongCollectionDAO();
    }

    @Override
    public List<SongCollection> findAllByLanguage(Language language) {
        return songCollectionDAO.findAllByLanguage(language);
    }
}
