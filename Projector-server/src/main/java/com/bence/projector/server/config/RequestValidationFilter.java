package com.bence.projector.server.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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

/**
 * Filter to validate HTTP requests and handle malformed requests gracefully.
 * This prevents NullPointerException in Jetty's SessionHandler when URI is null.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Run before CORS filter
public class RequestValidationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        if (!(req instanceof HttpServletRequest request)) {
            chain.doFilter(req, res);
            return;
        }

        HttpServletResponse response = (HttpServletResponse) res;

        // Validate that the request has a valid URI
        String requestURI = request.getRequestURI();
        if (requestURI == null) {
            // Log the issue but don't print stack trace to avoid noise
            String method = request.getMethod();
            String remoteAddr = request.getRemoteAddr();
            System.out.println("WARN: Received request with null URI from " + remoteAddr + 
                             " (method: " + (method != null ? method : "null") + ")");
            
            // Return a 400 Bad Request response
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter().write("Bad Request: Invalid HTTP request");
            return;
        }

        // Validate that the request method is not null
        String method = request.getMethod();
        if (method == null) {
            String remoteAddr = request.getRemoteAddr();
            System.out.println("WARN: Received request with null method from " + remoteAddr + 
                             " (URI: " + requestURI + ")");
            
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("text/plain");
            response.getWriter().write("Bad Request: Invalid HTTP method");
            return;
        }

        // Reject WebDAV methods (PROPFIND, etc.) early to prevent Spring Security firewall exceptions
        // These are typically scanner probes and should be rejected silently
        if ("PROPFIND".equals(method) || "PROPPATCH".equals(method) || 
            "MKCOL".equals(method) || "COPY".equals(method) || 
            "MOVE".equals(method) || "LOCK".equals(method) || 
            "UNLOCK".equals(method)) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            response.setContentType("text/plain");
            response.getWriter().write("Method Not Allowed");
            return;
        }

        // Request is valid, continue with the filter chain
        chain.doFilter(req, res);
    }

}

