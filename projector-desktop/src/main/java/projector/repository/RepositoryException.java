package projector.repository;

import java.io.Serial;

public class RepositoryException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RepositoryException() {
        super();
    }

    public RepositoryException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public RepositoryException(String arg0) {
        super(arg0);
    }

}
