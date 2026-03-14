package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.NormalizedWordBunchCacheService;
import com.bence.projector.server.backend.service.ReviewedWordService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.utils.models.NormalizedWordBunch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.bence.projector.server.utils.SetLanguages.getNormalizedWordBunches;

/**
 * Implementation of NormalizedWordBunchCacheService.
 * Provides in-memory caching of normalized word bunches by language UUID.
 */
@Service
public class NormalizedWordBunchCacheServiceImpl implements NormalizedWordBunchCacheService {

    private final ConcurrentHashMap<String, List<NormalizedWordBunch>> wordBunchesCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> reviewedWordsSetCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<String, ReviewedWord>> reviewedWordMapCache = new ConcurrentHashMap<>();
    private final SongService songService;
    private final LanguageService languageService;
    private final ReviewedWordService reviewedWordService;

    @Autowired
    public NormalizedWordBunchCacheServiceImpl(
            @Lazy SongService songService,
            LanguageService languageService,
            ReviewedWordService reviewedWordService
    ) {
        this.songService = songService;
        this.languageService = languageService;
        this.reviewedWordService = reviewedWordService;
    }

    @Override
    public Set<String> getReviewedWordsSetForLanguage(Language language) {
        String languageUuid = language.getUuid();
        return reviewedWordsSetCache.computeIfAbsent(languageUuid, k -> {
            List<ReviewedWord> list = reviewedWordService.findAllByLanguage(language);
            Set<String> setResult = ConcurrentHashMap.newKeySet();
            Map<String, ReviewedWord> mapResult = new ConcurrentHashMap<>();
            for (ReviewedWord rw : list) {
                if (rw.getNormalizedWord() != null) {
                    setResult.add(rw.getNormalizedWord());
                }
                if (rw.getWord() != null) {
                    mapResult.putIfAbsent(rw.getWord(), rw);
                }
            }
            reviewedWordMapCache.put(languageUuid, mapResult);
            return setResult;
        });
    }

    @Override
    public Map<String, ReviewedWord> getReviewedWordMapForLanguage(Language language) {
        String languageUuid = language.getUuid();
        Map<String, ReviewedWord> cached = reviewedWordMapCache.get(languageUuid);
        if (cached != null) {
            return cached;
        }
        getReviewedWordsSetForLanguage(language);
        return reviewedWordMapCache.get(languageUuid);
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
    public void addReviewedWordToCache(Language language, ReviewedWord reviewedWord) {
        if (reviewedWord == null) {
            return;
        }
        if (reviewedWord.getNormalizedWord() != null) {
            addToReviewedWordsSet(language, reviewedWord.getNormalizedWord());
        }
        if (reviewedWord.getWord() != null) {
            addToReviewedWordMap(language, reviewedWord);
        }
    }

    @Override
    public void removeReviewedWordFromCache(Language language, ReviewedWord reviewedWord) {
        if (reviewedWord == null) {
            return;
        }
        if (reviewedWord.getNormalizedWord() != null) {
            removeFromReviewedWordsSet(language, reviewedWord.getNormalizedWord());
        }
        if (reviewedWord.getWord() != null) {
            removeFromReviewedWordMap(language, reviewedWord.getWord());
        }
    }

    @Override
    public void addToReviewedWordsSet(Language language, String normalizedWord) {
        if (language == null || normalizedWord == null) {
            return;
        }
        Set<String> cached = reviewedWordsSetCache.get(language.getUuid());
        if (cached != null) {
            cached.add(normalizedWord);
        }
    }

    @Override
    public void removeFromReviewedWordsSet(Language language, String normalizedWord) {
        if (language == null || normalizedWord == null) {
            return;
        }
        Set<String> cached = reviewedWordsSetCache.get(language.getUuid());
        if (cached != null) {
            cached.remove(normalizedWord);
        }
    }

    @Override
    public void addToReviewedWordMap(Language language, ReviewedWord reviewedWord) {
        if (language == null || reviewedWord == null || reviewedWord.getWord() == null) {
            return;
        }
        Map<String, ReviewedWord> cached = reviewedWordMapCache.get(language.getUuid());
        if (cached != null) {
            cached.put(reviewedWord.getWord(), reviewedWord);
        }
    }

    @Override
    public void removeFromReviewedWordMap(Language language, String word) {
        if (language == null || word == null) {
            return;
        }
        Map<String, ReviewedWord> cached = reviewedWordMapCache.get(language.getUuid());
        if (cached != null) {
            cached.remove(word);
        }
    }

    @Override
    public void clearCacheForLanguage(String languageUuid) {
        wordBunchesCache.remove(languageUuid);
        reviewedWordsSetCache.remove(languageUuid);
        reviewedWordMapCache.remove(languageUuid);
    }

    @Override
    public void updateCacheForLanguage(String languageUuid, List<NormalizedWordBunch> wordBunches) {
        wordBunchesCache.put(languageUuid, wordBunches);
    }
}
