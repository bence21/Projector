package com.bence.projector.server.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles exceptions that would otherwise be processed by DefaultHandlerExceptionResolver.
 * For HttpMessageNotWritableException, only writes an error response when the response is not
 * yet committed, avoiding IllegalStateException: ABORTED when the client has disconnected.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotWritableException.class)
    public void handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("WARN: HttpMessageNotWritableException for " + method + " " + uri + ": " + ex.getMessage());

        if (!response.isCommitted()) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Internal Server Error: response could not be serialized\"}");
        }
    }
}
