package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Language;

import java.util.List;

public interface LanguageService extends BaseService<Language> {
    long countSongsByLanguage(Language id);

    void sortBySize(List<Language> all);

    Language findOneByUuid(String uuid);

    List<Language> findAllDeleted();
}
