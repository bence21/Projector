package com.bence.projector.server.config;

import com.bence.projector.server.utils.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Applies optional {@code app.base-url} from Spring config (including
 * gitignored application-local.properties) onto {@link AppProperties}.
 */
@Configuration
public class AppPropertiesConfig {

    public AppPropertiesConfig(@Value("${app.base-url:}") String baseUrl) {
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            AppProperties.getInstance().setBaseUrlOverride(baseUrl.trim());
        }
    }
}
