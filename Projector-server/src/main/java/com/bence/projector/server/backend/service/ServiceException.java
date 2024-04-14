package com.bence.projector.server.backend.service;

import com.bence.projector.server.api.ResponseMessage;
import org.springframework.http.HttpStatus;

public class ServiceException extends RuntimeException {

    private transient ResponseMessage responseMessage;
    private HttpStatus httpStatus;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(ResponseMessage responseMessage) {
        super(responseMessage.getMessage());
        this.responseMessage = responseMessage;
    }

    public ServiceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        responseMessage = new ResponseMessage(message);
    }

    public ServiceException(ResponseMessage responseMessage, HttpStatus httpStatus) {
        super(responseMessage.getMessage());
        this.httpStatus = httpStatus;
        this.responseMessage = responseMessage;
    }

    public ResponseMessage getResponseMessage() {
        if (responseMessage == null) {
            return new ResponseMessage(getMessage());
        }
        return responseMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
