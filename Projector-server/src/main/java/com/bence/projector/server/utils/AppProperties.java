package com.bence.projector.server.utils;

import java.io.IOException;
import java.util.Properties;

public class AppProperties {

    private static AppProperties instance;
    private final Properties properties;
    /** Optional override from Spring (e.g. application-local.properties). */
    private String baseUrlOverride;

    private AppProperties() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("app.properties"));
        } catch (IOException ignored) {
        }
    }

    public static AppProperties getInstance() {
        if (instance == null) {
            instance = new AppProperties();
        }
        return instance;
    }

    public void setBaseUrlOverride(String baseUrlOverride) {
        this.baseUrlOverride = baseUrlOverride;
    }

    public String baseUrl() {
        if (baseUrlOverride != null && !baseUrlOverride.isEmpty()) {
            return baseUrlOverride;
        }
        String baseUrl = (String) properties.get("baseUrl");
        String port = System.getenv("PORT");
        if (port != null && !port.isEmpty() && baseUrl != null && baseUrl.contains("://localhost")) {
            return baseUrl.replaceFirst("^(https?://localhost):\\d+", "$1:" + port);
        }
        return baseUrl;
    }

    public String shortBaseUrl() {
        return (String) properties.get("shortBaseUrl");
    }

    public boolean useMoreMemory() {
        return properties.get("useMoreMemory").equals("true");
    }

    public boolean isProduction() {
        Object production = properties.get("production");
        if (production == null) {
            return true;
        }
        return production.equals("true");
    }

    public String adminEmail() {
        return (String) properties.get("adminEmail");
    }

    public String getYouTubeAPIKey() {
        return (String) properties.get("YouTubeAPIKey");
    }
}
