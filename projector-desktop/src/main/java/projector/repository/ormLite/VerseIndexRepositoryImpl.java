package projector.repository.ormLite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.misc.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.VerseIndex;
import projector.repository.RepositoryException;
import projector.repository.VerseIndexRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

class VerseIndexRepositoryImpl extends AbstractRepository<VerseIndex> implements VerseIndexRepository {

    private static final Logger LOG = LoggerFactory.getLogger(VerseIndexRepositoryImpl.class);
    private final Dao<VerseIndex, Long> dao;
    private BookRepositoryImpl bookRepository;
    private String simpleName = "";
    private BibleVerseRepositoryImpl bibleVerseRepository;

    VerseIndexRepositoryImpl() throws SQLException {
        super(VerseIndex.class, DatabaseHelper.getInstance().getVerseIndexDao());
        dao = DatabaseHelper.getInstance().getVerseIndexDao();
    }

    @Override
    public boolean deleteAll(List<VerseIndex> models) throws RepositoryException {
        try {
            TransactionManager.callInTransaction(DatabaseHelper.getInstance().getConnectionSource(),
                    (Callable<Void>) () -> {
                        for (VerseIndex verseIndex : models) {
                            dao.executeRaw("Delete from VERSEINDEX where bibleVerse_id = " + verseIndex.getBibleVerse().getId());
                        }
                        return null;
                    });
            return true;
        } catch (RepositoryException | SQLException e) {
            final String msg = "Could not delete all " + simpleName + "s";
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public boolean delete(VerseIndex verseIndex) throws RepositoryException {
        try {
            dao.executeRaw("Delete from VERSEINDEX where bibleVerse_id = " + verseIndex.getBibleVerse().getId());
            return true;
        } catch (SQLException e) {
            String msg = "Could not delete " + simpleName;
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<VerseIndex> create(List<VerseIndex> verseIndices) {
        try {
            TransactionManager.callInTransaction(DatabaseHelper.getInstance().getConnectionSource(),
                    (Callable<Void>) () -> {
                        if (verseIndices.size() > 0) {
                            Long bibleId = verseIndices.get(0).getBibleVerse().getChapter().getBook().getBible().getId();
                            for (VerseIndex verseIndex : verseIndices) {
                                dao.executeRaw("INSERT INTO VERSEINDEX (INDEXNUMBER,BIBLEVERSE_ID,bibleId) VALUES ("
                                        + verseIndex.getIndexNumber()
                                        + "," + verseIndex.getBibleVerse().getId()
                                        + "," + bibleId
                                        + ")");
                            }
                        }
                        return null;
                    });
        } catch (SQLException e) {
            String msg = "Could not save verseIndices";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
        return verseIndices;
    }

    @Override
    public List<VerseIndex> findByIndex(Long index) {
        String msg = "Could not find index";
        try {
            List<VerseIndex> verseIndices = dao.queryForEq("indexNumber", index);
            for (VerseIndex verseIndex : verseIndices) {
                Chapter chapter = verseIndex.getBibleVerse().getChapter();
                Book book = getBookRepository().findById(chapter.getBook().getId());
                chapter.setBook(book);
            }
            return verseIndices;
        } catch (Exception e) {
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<BibleVerse> findByIndexAndBibleId(Long index, Long bibleId) {
        String msg = "Could not find index";
        try {
            GenericRawResults<Object[]> rawResults =
                    dao.queryRaw(
                            "SELECT BIBLEVERSE_ID  FROM VERSEINDEX where BibleId=" + bibleId + " and indexNumber=" + index,
                            new DataType[]{DataType.LONG});
            BibleVerseRepositoryImpl bibleVerseRepository = getBibleVerseRepository();
            List<BibleVerse> bibleVerses = new ArrayList<>();
            for (Object[] resultArray : rawResults) {
                Long id = (Long) resultArray[0];
                BibleVerse byId = bibleVerseRepository.findById(id);
                bibleVerses.add(byId);
            }
            rawResults.close();
            return bibleVerses;
        } catch (Exception e) {
            LOG.error(msg);
            throw new RepositoryException(msg, e);
        }
    }

    private BookRepositoryImpl getBookRepository() {
        if (bookRepository == null) {
            try {
                bookRepository = new BookRepositoryImpl();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return bookRepository;
    }

    private BibleVerseRepositoryImpl getBibleVerseRepository() {
        if (bibleVerseRepository == null) {
            try {
                bibleVerseRepository = new BibleVerseRepositoryImpl();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return bibleVerseRepository;
    }
}
