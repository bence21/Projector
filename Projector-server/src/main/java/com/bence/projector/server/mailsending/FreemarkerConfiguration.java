package com.bence.projector.server.mailsending;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

@Configuration
public class FreemarkerConfiguration implements WebMvcConfigurer {
    public static final String NEW_SONG = "newSongPage";
    public static final String NEW_SUGGESTION = "newSuggestionPage";
    public static final String NEW_SONG_LINK = "newSongLinkPage";
    public static final String NEW_STACK = "newStackPage";
    public static final String COLLECTION_UPDATE = "collectionUpdatePage";
    public static final String FREEMARKER_NAME_REGISTRATION = "registrationPage";
    public static final String TOKEN_LINK_PAGE = "tokenLinkPage";
    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {"classpath:/resources/", "classpath:/static/",
            "classpath:/webapp/"};

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }

    @Bean
    public FreeMarkerViewResolver freemarkerViewResolver() {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setCache(true);
        resolver.setPrefix("");
        resolver.setSuffix(".ftl");
        return resolver;
    }
}
