package projector.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.SongVerse;
import projector.repository.DAOFactory;
import projector.repository.RepositoryException;
import projector.repository.SongVerseDAO;
import projector.service.ServiceException;
import projector.service.SongVerseService;

import java.util.List;

public class SongVerseServiceImpl extends AbstractBaseService<SongVerse> implements SongVerseService {

    private final static Logger LOG = LoggerFactory.getLogger(SongVerseServiceImpl.class);
    private SongVerseDAO songVerseDAO = DAOFactory.getInstance().getSongVerseDAO();

    public SongVerseServiceImpl() {
        super(DAOFactory.getInstance().getSongVerseDAO());
    }

    @Override
    public List<SongVerse> findAll() throws ServiceException {
        try {
            return songVerseDAO.findAll();
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public SongVerse create(SongVerse songVerse) throws ServiceException {
        try {
            return songVerseDAO.create(songVerse);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public SongVerse update(SongVerse songVerse) throws ServiceException {
        try {
            return songVerseDAO.create(songVerse);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(SongVerse songVerse) throws ServiceException {
        try {
            return songVerseDAO.delete(songVerse);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(List<SongVerse> songVerses) throws ServiceException {
        try {
            return songVerseDAO.deleteAll(songVerses);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }
}
