package projector.repository.ormLite;

import projector.model.Information;
import projector.repository.InformationDAO;

import java.sql.SQLException;

class InformationDAOImpl extends AbstractBaseRepository<Information> implements InformationDAO {

    InformationDAOImpl() throws SQLException {
        super(Information.class, DatabaseHelper.getInstance().getInformationDao());
    }
}
