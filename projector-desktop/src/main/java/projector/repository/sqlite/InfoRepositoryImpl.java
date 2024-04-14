package projector.repository.sqlite;

import projector.model.sqlite.Info;

import java.sql.SQLException;

public class InfoRepositoryImpl extends AbstractBaseRepository<Info> implements InfoRepository {

    public InfoRepositoryImpl() throws SQLException {
        super(Info.class, DatabaseHelper.getInstance().getInfoDao());
    }
}
