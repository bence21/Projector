package projector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Credentials {
    private static final String DEFAULT_DOMAIN = "localhost:8081";

    public static final String DOMAIN = resolveDomain();
    public static final String WWW_DOMAIN = DOMAIN;
    public static final String BASE_URL = "http://" + WWW_DOMAIN; // if it's with https then on other devices will not work properly
    public static final String BASE_URL_S = "http://" + WWW_DOMAIN;

    private static String resolveDomain() {
        Properties local = loadLocalProperties();
        String domain = trimToNull(local.getProperty("domain"));
        if (domain != null) {
            return domain;
        }
        String port = trimToNull(local.getProperty("port"));
        if (port == null) {
            port = trimToNull(System.getenv("PORT"));
        }
        if (port != null) {
            return "localhost:" + port;
        }
        return DEFAULT_DOMAIN;
    }

    private static Properties loadLocalProperties() {
        Properties properties = new Properties();
        try (InputStream in = Credentials.class.getResourceAsStream("/credentials-local.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException ignored) {
            // Fall back to env / default
        }
        return properties;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
