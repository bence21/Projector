package projector.service.sqlite;

import projector.repository.CrudDAO;
import projector.service.CrudService;
import projector.service.ServiceException;

import java.util.List;

public class AbstractBaseService<T> implements CrudService<T> {

    private final CrudDAO<T> crudDAO;

    AbstractBaseService(CrudDAO<T> crudDAO) {
        this.crudDAO = crudDAO;
    }

    @Override
    public List<T> findAll() throws ServiceException {
        return crudDAO.findAll();
    }

    @Override
    public T create(T t) throws ServiceException {
        return crudDAO.create(t);
    }

    @Override
    public List<T> create(List<T> ts) throws ServiceException {
        return crudDAO.create(ts);
    }

    @Override
    public T update(T t) throws ServiceException {
        return crudDAO.update(t);
    }

    @Override
    public boolean delete(T t) throws ServiceException {
        return crudDAO.delete(t);
    }

    @Override
    public boolean delete(List<T> ts) throws ServiceException {
        return crudDAO.deleteAll(ts);
    }

    @Override
    public T findByUuid(String uuid) {
        if (uuid == null) {
            return null;
        }
        return crudDAO.findByUuid(uuid);
    }

    @Override
    public T findById(Long id) {
        return crudDAO.findById(id);
    }
}
