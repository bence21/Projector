package com.bence.projector.server.config;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configuration for Jetty server to handle malformed requests gracefully.
 * This prevents NullPointerException warnings when requests with null URIs are received.
 */
@Configuration
public class JettyConfig {

    @Bean
    public WebServerFactoryCustomizer<JettyServletWebServerFactory> jettyCustomizer() {
        return factory -> factory.addServerCustomizers(server -> {
            // Set a custom error handler
            server.setErrorHandler(new CustomErrorHandler());

            // Wrap the handler chain with a request validator
            Handler handler = server.getHandler();
            if (handler != null) {
                server.setHandler(new RequestValidationHandlerWrapper(handler));
            }
        });
    }

    /**
     * Handler wrapper that validates requests before they reach the SessionHandler.
     * This prevents NullPointerException in Jetty's SessionHandler when URI is null.
     */
    private static class RequestValidationHandlerWrapper extends HandlerWrapper {
        public RequestValidationHandlerWrapper(Handler handler) {
            setHandler(handler);
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            // Validate request URI before processing
            String requestURI = request.getRequestURI();
            if (requestURI == null) {
                // Log without stack trace to avoid noise
                String method = request.getMethod();
                String remoteAddress = request.getRemoteAddr();
                System.out.println("WARN: Rejected request with null URI from " + remoteAddress +
                        " (method: " + (method != null ? method : "null") + ")");

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().write("Bad Request: Invalid HTTP request");
                baseRequest.setHandled(true);
                return;
            }

            // Request is valid, continue with the handler chain
            try {
                super.handle(target, baseRequest, request, response);
            } catch (NullPointerException e) {
                // Catch NullPointerException that might occur in SessionHandler
                if (e.getMessage() != null && e.getMessage().contains("uri") && e.getMessage().contains("null")) {
                    System.out.println("WARN: Caught NullPointerException for null URI: " + e.getMessage());
                    if (!response.isCommitted()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType("text/plain");
                        response.getWriter().write("Bad Request: Invalid HTTP request");
                        baseRequest.setHandled(true);
                    }
                } else {
                    // Re-throw if it's a different NullPointerException
                    throw e;
                }
            }
        }
    }

    /**
     * Custom error handler that handles errors gracefully.
     */
    private static class CustomErrorHandler extends ErrorHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException, ServletException {
            // Check if this is a null URI issue
            if (request.getRequestURI() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().write("Bad Request: Invalid HTTP request");
                baseRequest.setHandled(true);
                return;
            }

            // For other errors, use default handling
            super.handle(target, baseRequest, request, response);
        }
    }
}

