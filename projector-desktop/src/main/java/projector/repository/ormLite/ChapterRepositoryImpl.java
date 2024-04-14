package projector.repository.ormLite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Chapter;
import projector.repository.ChapterRepository;
import projector.repository.RepositoryException;

import java.sql.SQLException;
import java.util.List;

public class ChapterRepositoryImpl extends AbstractBaseRepository<Chapter> implements ChapterRepository {

    private static final Logger LOG = LoggerFactory.getLogger(BookRepositoryImpl.class);
    private BibleVerseRepositoryImpl bibleVerseRepository;

    ChapterRepositoryImpl() throws SQLException {
        super(Chapter.class, DatabaseHelper.getInstance().getChapterDao());
        try {
            bibleVerseRepository = new BibleVerseRepositoryImpl();
        } catch (SQLException e) {
            String msg = "Failed to initialize";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public boolean delete(Chapter model) throws RepositoryException {
        super.delete(model);
        bibleVerseRepository.deleteAll(model.getVerses());
        return true;
    }

    @Override
    public boolean deleteAll(List<Chapter> models) throws RepositoryException {
        for (Chapter chapter : models) {
            delete(chapter);
        }
        return true;
    }
}
