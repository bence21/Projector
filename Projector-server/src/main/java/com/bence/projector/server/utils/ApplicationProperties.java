package com.bence.projector.server.utils;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationProperties {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^}:]+)(?::([^}]*))?}");

    private static ApplicationProperties instance;
    private final Properties properties;
    /** Optional overrides from Spring (resolved placeholders + local config). */
    private String springDatasourceUrlOverride;
    private String springDatasourceUsernameOverride;
    private String springDatasourcePasswordOverride;

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

    public void setSpringDatasourceUrlOverride(String springDatasourceUrlOverride) {
        this.springDatasourceUrlOverride = springDatasourceUrlOverride;
    }

    public void setSpringDatasourceUsernameOverride(String springDatasourceUsernameOverride) {
        this.springDatasourceUsernameOverride = springDatasourceUsernameOverride;
    }

    public void setSpringDatasourcePasswordOverride(String springDatasourcePasswordOverride) {
        this.springDatasourcePasswordOverride = springDatasourcePasswordOverride;
    }

    public String springDatasourceUrl() {
        if (springDatasourceUrlOverride != null && !springDatasourceUrlOverride.isEmpty()) {
            return springDatasourceUrlOverride;
        }
        return resolvePlaceholders((String) properties.get("spring.datasource.url"));
    }

    public String springDatasourceUsername() {
        if (springDatasourceUsernameOverride != null && !springDatasourceUsernameOverride.isEmpty()) {
            return springDatasourceUsernameOverride;
        }
        return resolvePlaceholders((String) properties.get("spring.datasource.username"));
    }

    public String springDatasourcePassword() {
        if (springDatasourcePasswordOverride != null && !springDatasourcePasswordOverride.isEmpty()) {
            return springDatasourcePasswordOverride;
        }
        return resolvePlaceholders((String) properties.get("spring.datasource.password"));
    }

    /**
     * Resolves {@code ${ENV:default}} / {@code ${ENV}} the same way Spring Boot does for
     * env vars, so JDBC URLs work even before Spring injects overrides.
     */
    static String resolvePlaceholders(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }
        Matcher matcher = PLACEHOLDER.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);
            String defaultValue = matcher.group(2);
            String env = System.getenv(name);
            if (env == null || env.isEmpty()) {
                env = System.getProperty(name);
            }
            String replacement = (env != null && !env.isEmpty())
                    ? env
                    : (defaultValue != null ? defaultValue : matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
