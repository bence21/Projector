package projector.config;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import projector.utils.AppProperties;

public class Log4j2Config {

    private static Log4j2Config instance;

    private Log4j2Config() {
    }

    public static Log4j2Config getInstance() {
        if (instance == null) {
            instance = new Log4j2Config();
        }
        return instance;
    }

    public void initializeLog4j2OnMac() {
        AppProperties appProperties = AppProperties.getInstance();
        if (!appProperties.isMacOs()) {
            return;
        }
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.add(builder.newAppender("file", "File")
                .addAttribute("fileName", getLogFilePath())
                .add(builder.newLayout("PatternLayout")
                        .addAttribute("pattern", "[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %c{1} - %msg%n")));
        builder.add(builder.newLogger("projector", "debug")
                .add(builder.newAppenderRef("file"))
                .addAttribute("additivity", false));
        builder.add(builder.newRootLogger(org.apache.logging.log4j.Level.INFO)
                .add(builder.newAppenderRef("file")));
        Configuration configuration = builder.build();

        // Reconfigure Log4j with the new Configuration
        Configurator.initialize(configuration);
    }

    public String getLogFilePath() {
        return AppProperties.getInstance().getWorkDirectory() + "projector.log";
    }
}
