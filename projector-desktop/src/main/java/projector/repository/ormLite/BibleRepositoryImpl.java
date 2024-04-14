package projector.repository.ormLite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.VerseIndex;
import projector.repository.BibleRepository;
import projector.repository.BookRepository;
import projector.repository.RepositoryException;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class BibleRepositoryImpl extends AbstractBaseRepository<Bible> implements BibleRepository {
    private static final Logger LOG = LoggerFactory.getLogger(BibleRepositoryImpl.class);
    private final VerseIndexRepositoryImpl verseIndexRepository;
    private final BibleVerseRepositoryImpl bibleVerseRepository;
    private final BookRepository bookRepository;

    BibleRepositoryImpl() throws SQLException {
        super(Bible.class, DatabaseHelper.getInstance().getBibleDao());
        try {
            bookRepository = new BookRepositoryImpl();
            bibleVerseRepository = new BibleVerseRepositoryImpl();
            verseIndexRepository = new VerseIndexRepositoryImpl();
        } catch (SQLException e) {
            String msg = "Failed to initialize BibleRepository";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public Bible create(Bible bible) {
        Bible collection = super.create(bible);
        bookRepository.create(bible.getBooks());
        List<BibleVerse> verses = new LinkedList<>();
        for (Book book : bible.getBooks()) {
            for (Chapter chapter : book.getChapters()) {
                verses.addAll(chapter.getVerses());
            }
        }
        bibleVerseRepository.create(verses);
        List<VerseIndex> verseIndices = new LinkedList<>();
        for (BibleVerse bibleVerse : verses) {
            List<VerseIndex> verseIndexList = bibleVerse.getVerseIndices();
            if (verseIndexList != null) {
                verseIndices.addAll(verseIndexList);
            }
        }
        verseIndexRepository.create(verseIndices);
        return collection;
    }

    @Override
    public List<Bible> create(List<Bible> bibles) {
        for (Bible bible : bibles) {
            create(bible);
        }
        return bibles;
    }

    @Override
    public boolean delete(Bible model) throws RepositoryException {
        super.delete(model);
        bookRepository.deleteAll(model.getBooks());
        return true;
    }

    @Override
    public boolean deleteAll(List<Bible> models) throws RepositoryException {
        for (Bible bible : models) {
            delete(bible);
        }
        return true;
    }

    @Override
    public Bible update(Bible model) throws RepositoryException {
        return super.create(model);
    }

    @Override
    public void update(List<Bible> models) {
        for (Bible bible : models) {
            update(bible);
        }
    }
}
