package projector.service;

import java.util.List;

public interface CrudService<T> {

    List<T> findAll() throws ServiceException;

    T create(T t) throws ServiceException;

    List<T> create(List<T> ts) throws ServiceException;

    T update(T t) throws ServiceException;

    boolean delete(T t) throws ServiceException;

    boolean delete(List<T> ts) throws ServiceException;

    T findByUuid(String uuid);

    T findById(Long id);
}
