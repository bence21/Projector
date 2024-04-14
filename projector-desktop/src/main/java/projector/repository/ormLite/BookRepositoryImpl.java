package projector.repository.ormLite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.Book;
import projector.repository.BookRepository;
import projector.repository.ChapterRepository;
import projector.repository.RepositoryException;

import java.sql.SQLException;
import java.util.List;

class BookRepositoryImpl extends AbstractBaseRepository<Book> implements BookRepository {

    private static final Logger LOG = LoggerFactory.getLogger(BookRepositoryImpl.class);
    private final ChapterRepository chapterRepository;

    BookRepositoryImpl() throws SQLException {
        super(Book.class, DatabaseHelper.getInstance().getBookDao());
        try {
            chapterRepository = new ChapterRepositoryImpl();
        } catch (SQLException e) {
            String msg = "Failed to initialize BookRepository";
            LOG.error(msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public Book create(Book book) {
        Book collection = super.create(book);
        chapterRepository.create(book.getChapters());
        return collection;
    }

    @Override
    public List<Book> create(List<Book> books) {
        for (Book book : books) {
            create(book);
        }
        return books;
    }

    @Override
    public boolean delete(Book model) throws RepositoryException {
        super.delete(model);
        chapterRepository.deleteAll(model.getChapters());
        return true;
    }

    @Override
    public boolean deleteAll(List<Book> models) throws RepositoryException {
        for (Book book : models) {
            delete(book);
        }
        return true;
    }
}
