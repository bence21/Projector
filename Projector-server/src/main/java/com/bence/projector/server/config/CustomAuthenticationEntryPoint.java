package com.bence.projector.server.config;

import org.eclipse.jetty.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (authException != null && "Full authentication is required to access this resource".equals(authException.getMessage())) {
            response.setStatus(HttpStatus.UNAUTHORIZED_401);
        }
    }
}
