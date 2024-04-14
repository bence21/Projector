package projector.service;

import java.io.Serial;

public class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceException() {
        super();
    }

    public ServiceException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public ServiceException(String arg0) {
        super(arg0);
    }

    public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

}
