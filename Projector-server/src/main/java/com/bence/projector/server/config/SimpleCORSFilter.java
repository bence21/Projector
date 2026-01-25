package com.bence.projector.server.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // Run after RequestValidationFilter
public class SimpleCORSFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        if (!(req instanceof HttpServletRequest request)) {
            chain.doFilter(req, res);
            return;
        }

        HttpServletResponse response = (HttpServletResponse) res;

        // Validate request before processing
        String requestURI = request.getRequestURI();
        if (requestURI == null) {
            // Request validation filter should have caught this, but handle it here as well
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter().write("Bad Request: Invalid HTTP request");
            return;
        }

        final String allowedOrigin = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Origin", allowedOrigin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "86400");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");

        try {
            chain.doFilter(req, res);
        } catch (RequestRejectedException e) {
            String message = e.getMessage();
            // Suppress logging for PROPFIND and other WebDAV methods (scanner probes)
            if (message != null && (message.contains("PROPFIND") || 
                message.contains("PROPPATCH") || message.contains("MKCOL") ||
                message.contains("COPY") || message.contains("MOVE") ||
                message.contains("LOCK") || message.contains("UNLOCK"))) {
                // Silently reject WebDAV methods with HTTP 405 (Method Not Allowed)
                response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            } else {
                // Log other RequestRejectedException errors
                System.out.println("RequestRejectedException: " + message);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (IllegalStateException e) {
            System.out.println("IllegalStateException: " + e.getMessage());
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (NullPointerException e) {
            // Log with more context
            System.out.println("NullPointerException in filter chain: " + e.getMessage() + 
                             " (URI: " + requestURI + ", Method: " + request.getMethod() + ")");
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

}
