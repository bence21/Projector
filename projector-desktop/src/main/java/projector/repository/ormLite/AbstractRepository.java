package projector.repository.ormLite;

import com.j256.ormlite.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.repository.CrudDAO;
import projector.repository.RepositoryException;

import java.sql.SQLException;
import java.util.List;

public abstract class AbstractRepository<T> implements CrudDAO<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRepository.class);

    Dao<T, Long> dao;
    private String simpleName;
    private String simpleNames;

    AbstractRepository(Class<T> clazz, Dao<T, Long> dao) {
        this.dao = dao;
        simpleName = clazz.getSimpleName();
        simpleNames = simpleName + "s";
    }

    @Override
    public T findById(final Long id) throws RepositoryException {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            String msg = "Could not find " + simpleName;
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<T> findAll() {
        String msg = "Could not find all " + simpleNames;
        try {
            return dao.callBatchTasks(
                    () -> dao.queryForAll());
        } catch (final SQLException e) {
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public T create(T model) throws RepositoryException {
        try {
            dao.createOrUpdate(model);
            return model;
        } catch (SQLException e) {
            String msg = "Could not save " + simpleName;
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<T> create(List<T> models) throws RepositoryException {
        try {
            for (final T model : models) {
                dao.createOrUpdate(model);
            }
            return models;
        } catch (SQLException e) {
            String msg = "Could not save " + simpleNames;
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public T update(T model) throws RepositoryException {
        return create(model);
    }

    @Override
    public void update(List<T> models) {
        try {
            create(models);
        } catch (RepositoryException e) {
            String msg = "Could not update " + simpleName;
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public T findByUuid(String uuid) {
        String msg = "Could not find song";
        try {
            List<T> uuid1 = dao.queryForEq("uuid", uuid);
            if (uuid1 != null && uuid1.size() > 0) {
                return uuid1.get(0);
            }
            return null;
        } catch (Exception e) {
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }
}
