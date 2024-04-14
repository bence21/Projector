package projector.repository.ormLite;

import com.j256.ormlite.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.BaseEntity;
import projector.repository.CrudDAO;
import projector.repository.RepositoryException;

import java.sql.SQLException;
import java.util.List;

public class AbstractBaseRepository<T extends BaseEntity> extends AbstractRepository<T> implements CrudDAO<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBaseRepository.class);

    Dao<T, Long> dao;
    private String simpleName;
    private String simpleNames;

    AbstractBaseRepository(Class<T> clazz, Dao<T, Long> dao) {
        super(clazz, dao);
        this.dao = dao;
        simpleName = clazz.getSimpleName();
        simpleNames = simpleName + "s";
    }

    @Override
    public boolean delete(T model) throws RepositoryException {
        try {
            dao.deleteById(model.getId());
            return true;
        } catch (SQLException e) {
            String msg = "Could not delete " + simpleName;
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public boolean deleteAll(List<T> models) throws RepositoryException {
        try {
            for (T model : models) {
                delete(model);
            }
            return true;
        } catch (RepositoryException e) {
            final String msg = "Could not delete all " + simpleNames;
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }
}
