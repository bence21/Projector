package projector.service.impl;

import projector.model.Information;
import projector.repository.DAOFactory;
import projector.repository.InformationDAO;
import projector.repository.RepositoryException;
import projector.service.InformationService;
import projector.service.ServiceException;

import java.util.List;

public class InformationServiceImpl extends AbstractBaseService<Information> implements InformationService {

    private final InformationDAO informationDAO = DAOFactory.getInstance().getInformationDAO();

    public InformationServiceImpl() {
        super(DAOFactory.getInstance().getInformationDAO());
    }

    @Override
    public List<Information> findAll() throws ServiceException {
        try {
            return informationDAO.findAll();
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Information create(Information information) throws ServiceException {
        try {
            return informationDAO.create(information);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Information update(Information information) throws ServiceException {
        return null;
    }

    @Override
    public boolean delete(Information information) throws ServiceException {
        try {
            return informationDAO.delete(information);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(List<Information> information) throws ServiceException {
        try {
            return informationDAO.deleteAll(information);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Information findFirst() throws ServiceException {
        try {
            return informationDAO.findById(1L);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }
}
