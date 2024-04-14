package projector.repository.ormLite;

import projector.model.CountdownTime;
import projector.repository.CountdownTimeRepository;

import java.sql.SQLException;

public class CountdownTimeRepositoryImpl extends AbstractBaseRepository<CountdownTime> implements CountdownTimeRepository {

    CountdownTimeRepositoryImpl() throws SQLException {
        super(CountdownTime.class, DatabaseHelper.getInstance().getCountdownTimeDao().getDao());
    }

}
