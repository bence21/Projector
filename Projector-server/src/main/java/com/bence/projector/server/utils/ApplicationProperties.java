package com.bence.projector.server.utils;

import java.io.IOException;
import java.util.Properties;

public class ApplicationProperties {

    private static ApplicationProperties instance;
    private final Properties properties;

    private ApplicationProperties() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException ignored) {
        }
    }

    public static ApplicationProperties getInstance() {
        if (instance == null) {
            instance = new ApplicationProperties();
        }
        return instance;
    }

    public String springDatasourceUrl() {
        return (String) properties.get("spring.datasource.url");
    }

    public String springDatasourceUsername() {
        return (String) properties.get("spring.datasource.username");
    }

    public String springDatasourcePassword() {
        return (String) properties.get("spring.datasource.password");
    }
}
