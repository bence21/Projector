package com.bence.projector.server.config;

import com.bence.projector.server.utils.ApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Applies Spring-resolved datasource settings (including
 * {@code application-local.properties} and {@code ${MYSQL_PORT:3306}}) onto
 * {@link ApplicationProperties} used by {@code QueryUtil}.
 */
@Configuration
public class ApplicationPropertiesConfig {

    public ApplicationPropertiesConfig(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {
        ApplicationProperties properties = ApplicationProperties.getInstance();
        properties.setSpringDatasourceUrlOverride(url);
        properties.setSpringDatasourceUsernameOverride(username);
        properties.setSpringDatasourcePasswordOverride(password);
    }
}
