package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.ReviewedWordService;
import com.bence.projector.server.backend.service.SongService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Covers null-safe reviewed-word map lookup (avoids NPE when language context is absent).
 */
@RunWith(MockitoJUnitRunner.class)
public class NormalizedWordBunchCacheServiceImplReviewedWordMapTest {

    @Mock
    private SongService songService;
    @Mock
    private LanguageService languageService;
    @Mock
    private ReviewedWordService reviewedWordService;

    @Test
    public void getReviewedWordMapForLanguage_nullLanguage_returnsEmptyMap() {
        NormalizedWordBunchCacheServiceImpl cache =
                new NormalizedWordBunchCacheServiceImpl(songService, languageService, reviewedWordService);
        Assert.assertTrue(cache.getReviewedWordMapForLanguage(null).isEmpty());
    }

    @Test
    public void getReviewedWordMapForLanguage_languageWithoutUuid_returnsEmptyMap() {
        NormalizedWordBunchCacheServiceImpl cache =
                new NormalizedWordBunchCacheServiceImpl(songService, languageService, reviewedWordService);
        Language language = new Language();
        language.setUuid(null);
        Assert.assertTrue(cache.getReviewedWordMapForLanguage(language).isEmpty());
    }
}
