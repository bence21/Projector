package projector.service.impl;

import projector.model.Book;
import projector.repository.DAOFactory;
import projector.service.BookService;

public class BookServiceImpl extends AbstractBaseService<Book> implements BookService {

    public BookServiceImpl() {
        super(DAOFactory.getInstance().getBookDAO());
    }
}
