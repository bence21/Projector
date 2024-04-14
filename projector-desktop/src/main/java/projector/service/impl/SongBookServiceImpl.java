package projector.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.SongBook;
import projector.repository.DAOFactory;
import projector.repository.RepositoryException;
import projector.repository.SongBookDAO;
import projector.service.ServiceException;
import projector.service.SongBookService;

import java.util.List;

public class SongBookServiceImpl extends AbstractBaseService<SongBook> implements SongBookService {

    private final static Logger LOG = LoggerFactory.getLogger(SongBookServiceImpl.class);
    private SongBookDAO songBookDAO = DAOFactory.getInstance().getSongBookDAO();

    public SongBookServiceImpl() {
        super(DAOFactory.getInstance().getSongBookDAO());
    }

    @Override
    public List<SongBook> findAll() throws ServiceException {
        try {
            return songBookDAO.findAll();
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public SongBook create(SongBook songBook) throws ServiceException {
        try {
            return songBookDAO.create(songBook);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public SongBook update(SongBook songBook) throws ServiceException {
        try {
            return songBookDAO.update(songBook);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(SongBook songBook) throws ServiceException {
        try {
            return songBookDAO.delete(songBook);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(List<SongBook> songBooks) throws ServiceException {
        try {
            return songBookDAO.deleteAll(songBooks);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }
}
