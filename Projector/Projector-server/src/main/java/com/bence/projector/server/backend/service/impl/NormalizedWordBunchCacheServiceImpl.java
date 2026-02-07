package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.NormalizedWordBunchCacheService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.utils.models.NormalizedWordBunch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.bence.projector.server.utils.SetLanguages.getNormalizedWordBunches;

/**
 * Implementation of NormalizedWordBunchCacheService.
 * Provides in-memory caching of normalized word bunches by language UUID.
 */
@Service
public class NormalizedWordBunchCacheServiceImpl implements NormalizedWordBunchCacheService {

    private final ConcurrentHashMap<String, List<NormalizedWordBunch>> wordBunchesCache = new ConcurrentHashMap<>();
    private final SongService songService;
    private final LanguageService languageService;

    @Autowired
    public NormalizedWordBunchCacheServiceImpl(
            SongService songService,
            LanguageService languageService
    ) {
        this.songService = songService;
        this.languageService = languageService;
    }

    @Override
    public List<NormalizedWordBunch> getAllWordBunchesForLanguage(Language language) {
        String languageUuid = language.getUuid();
        // Check cache first
        List<NormalizedWordBunch> cached = wordBunchesCache.get(languageUuid);
        if (cached != null) {
            return cached;
        }
        // Compute and cache
        List<NormalizedWordBunch> result = getNormalizedWordBunches(
                songService.findAllByLanguage(languageUuid),
                languageService.findAll(),
                language
        );
        wordBunchesCache.put(languageUuid, result);
        return result;
    }

    @Override
    public void clearCacheForLanguage(String languageUuid) {
        wordBunchesCache.remove(languageUuid);
    }

    @Override
    public void updateCacheForLanguage(String languageUuid, List<NormalizedWordBunch> wordBunches) {
        wordBunchesCache.put(languageUuid, wordBunches);
    }
}
