package projector.repository;

import java.util.List;

public interface CrudDAO<T> {
    List<T> findAll() throws RepositoryException;

    T findById(Long id) throws RepositoryException;

    T create(T t) throws RepositoryException;

    T update(T t) throws RepositoryException;

    List<T> create(List<T> models) throws RepositoryException;

    boolean delete(T t) throws RepositoryException;

    boolean deleteAll(List<T> models) throws RepositoryException;

    void update(List<T> models);

    T findByUuid(String uuid);
}
