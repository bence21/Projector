package projector.repository.sqlite;

import projector.model.sqlite.Books;

import java.sql.SQLException;

public class BooksRepositoryImpl extends AbstractBaseRepository<Books> implements BooksRepository {

    public BooksRepositoryImpl() throws SQLException {
        super(Books.class, DatabaseHelper.getInstance().getBooksDao());
    }
}
