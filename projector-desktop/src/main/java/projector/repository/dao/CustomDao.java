package projector.repository.dao;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings("ClassCanBeRecord")
public class CustomDao<T, ID> {
    private final Dao<T, ID> dao;

    public CustomDao(Dao<T, ID> dao) {
        this.dao = dao;
    }

    public Dao<T, ID> getDao() {
        return dao;
    }

    public void executeRaw(String s) throws SQLException {
        dao.executeRaw(s);
    }

    public ArrayList<T> queryForEq(String fieldName, Object value) throws SQLException {
        if (value == null) {
            return null;
        }
        return (ArrayList<T>) dao.queryForEq(fieldName, value);
    }

    public T queryForId(ID id) throws SQLException {
        return dao.queryForId(id);
    }

    public List<T> queryForAll() throws SQLException {
        return dao.queryForAll();
    }

    public void createOrUpdate(T t) throws SQLException {
        dao.createOrUpdate(t);
    }

    public <CT> CT callBatchTasks(Callable<CT> voidCallable) throws Exception {
        return dao.callBatchTasks(voidCallable);
    }

    public void deleteById(ID id) throws SQLException {
        dao.deleteById(id);
    }

    public long queryRawValue(String s) throws SQLException {
        return dao.queryRawValue(s);
    }
}
