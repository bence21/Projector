package projector.repository.sqlite;

import projector.model.sqlite.Verses;

import java.sql.SQLException;

public class VersesRepositoryImpl extends AbstractBaseRepository<Verses> implements VersesRepository {

    public VersesRepositoryImpl() throws SQLException {
        super(Verses.class, DatabaseHelper.getInstance().getVersesDao());
    }
}
