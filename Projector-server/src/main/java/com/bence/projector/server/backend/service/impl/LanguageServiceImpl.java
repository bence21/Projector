package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.repository.LanguageRepository;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.utils.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LanguageServiceImpl extends BaseServiceImpl<Language> implements LanguageService {
    private final ConcurrentHashMap<String, Language> languageMap = new ConcurrentHashMap<>();
    @Autowired
    private LanguageRepository languageRepository;
    @Autowired
    private SongRepository songRepository;

    @Override
    public long countSongsByLanguage(Language language) {
        if (language != null) {
            return songRepository.countAllByLanguageAndIsBackUpIsNullAndDeletedIsFalse(language);
        }
        return 0L;
    }

    @Override
    public void sortBySize(List<Language> languages) {
        for (Language language : languages) {
            language.setSongsCount(countSongsByLanguage(language));
        }
        languages.sort((o1, o2) -> Long.compare(o2.getSongsCount(), o1.getSongsCount()));
    }

    @Override
    public List<Language> findAll() {
        Iterable<Language> languages = languageRepository.findAll();
        List<Language> allLanguages = new ArrayList<>();
        for (Language language : languages) {
            if (!language.isDeleted()) {
                allLanguages.add(findOneByUuid(language.getUuid()));
            }
        }
        return allLanguages;
    }

    @Override
    public Language findOneByUuid(String id) {
        if (languageMap.containsKey(id)) {
            return languageMap.get(id);
        }
        Language language = languageRepository.findOneByUuid(id);
        if (AppProperties.getInstance().useMoreMemory()) {
            languageMap.put(id, language);
        }
        return language;
    }

    @Override
    public List<Language> findAllDeleted() {
        Iterable<Language> languages = languageRepository.findAll();
        List<Language> deletedLanguages = new ArrayList<>();
        for (Language language : languages) {
            if (language.isDeleted()) {
                deletedLanguages.add(findOneByUuid(language.getUuid()));
            }
        }
        return deletedLanguages;
    }
}
