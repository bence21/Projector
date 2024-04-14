package com.bence.projector.server.mailsending;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public class ConfigurationUtil {
    private static Configuration configuration;

    static {
        configuration = new Configuration(Configuration.VERSION_2_3_31);
        ClassTemplateLoader loader = new ClassTemplateLoader(ConfigurationUtil.class, "/");
        configuration.setTemplateLoader(loader);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public static Configuration getConfiguration() {
        return configuration;
    }
}
