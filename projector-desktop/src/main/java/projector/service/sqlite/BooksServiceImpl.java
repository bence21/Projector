package projector.service.sqlite;

import projector.model.sqlite.Books;
import projector.repository.DAOFactory;

public class BooksServiceImpl extends AbstractBaseService<Books> implements BooksService {

    public BooksServiceImpl() {
        super(DAOFactory.getInstance().getBooksDAO());
    }
}
