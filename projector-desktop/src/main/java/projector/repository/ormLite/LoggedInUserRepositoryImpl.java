package projector.repository.ormLite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.LoggedInUser;
import projector.repository.LoggedInUserRepository;
import projector.repository.dao.CustomDao;

import java.sql.SQLException;
import java.util.ArrayList;

public class LoggedInUserRepositoryImpl extends AbstractBaseRepository<LoggedInUser> implements LoggedInUserRepository {

    private static final Logger LOG = LoggerFactory.getLogger(LoggedInUserRepositoryImpl.class);
    private final CustomDao<LoggedInUser, Long> loggedInUserDao;

    LoggedInUserRepositoryImpl() throws SQLException {
        super(LoggedInUser.class, DatabaseHelper.getInstance().getLoggedInUserDao().getDao());
        loggedInUserDao = DatabaseHelper.getInstance().getLoggedInUserDao();
    }

    @Override
    public LoggedInUser findByEmail(String email) {
        try {
            ArrayList<LoggedInUser> loggedInUsersByEmail = loggedInUserDao.queryForEq("email", email);
            if (loggedInUsersByEmail == null || loggedInUsersByEmail.size() == 0) {
                return null;
            }
            return loggedInUsersByEmail.get(0);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }
}
