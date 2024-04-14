package projector.repository.sqlite;

import com.j256.ormlite.dao.Dao;
import projector.repository.CrudDAO;
import projector.repository.RepositoryException;

import java.sql.SQLException;
import java.util.List;

public class AbstractBaseRepository<T> extends AbstractRepository<T> implements CrudDAO<T> {

    Dao<T, Long> dao;

    AbstractBaseRepository(Class<T> clazz, Dao<T, Long> dao) {
        super(clazz, dao);
        this.dao = dao;
    }

    @Override
    public boolean delete(T t) throws RepositoryException {
        try {
            int delete = dao.delete(t);
            return delete > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteAll(List<T> models) throws RepositoryException {
        return false;
    }
}
