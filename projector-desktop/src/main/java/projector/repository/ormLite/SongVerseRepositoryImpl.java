package projector.repository.ormLite;

import projector.model.SongVerse;
import projector.repository.SongVerseDAO;

import java.sql.SQLException;

public class SongVerseRepositoryImpl extends AbstractBaseRepository<SongVerse> implements SongVerseDAO {

    public SongVerseRepositoryImpl() throws SQLException {
        super(SongVerse.class, DatabaseHelper.getInstance().getSongVerseDao());
    }
}
