package projector.utils;

import java.io.IOException;
import java.util.Properties;

public class AppProperties {

    private static AppProperties instance;
    private final Properties properties;

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

    public boolean isMacOs() {
        String osName = System.getProperty("os.name");
        return osName.startsWith("Mac OS X") || osName.startsWith("Mac OS");
    }

    private String getDatabaseFolder_() {
        Object database = properties.get("database");
        if (database == null || ((String) database).isEmpty()) {
            return "data";
        }
        return (String) database;
    }

    public String getDatabaseFolder() {
        return getWorkDirectory() + getDatabaseFolder_();
    }

    public String getWorkDirectory() {
        if (isMacOs()) {
            String macWorkDirectoryName = (String) properties.get("macWorkDirectoryName");
            if (macWorkDirectoryName != null) {
                String workDirectoryPath = System.getProperty("user.home") + "/Library/Application Support/";
                return workDirectoryPath + macWorkDirectoryName + "/";
            }
        }
        return "./";
    }

    public String getAppStatePath() {
        return getWorkDirectory() + "appState.json";
    }
}
