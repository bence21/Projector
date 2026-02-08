package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import com.bence.projector.server.backend.model.User;

import java.util.List;

public interface ReviewedWordService extends BaseService<ReviewedWord> {

    List<ReviewedWord> findAllByLanguageAndStatus(Language language, ReviewedWordStatus status);

    List<ReviewedWord> findAllByLanguage(Language language);

    ReviewedWord findOneByUuid(String uuid);

    ReviewedWord saveOrUpdate(ReviewedWord reviewedWord, User reviewedBy);

    void saveBulkNewWords(List<String> words, Language language, User reviewedBy);

    void deleteReview(String uuid);

    /**
     * Detect potential source languages for a word based on where the same (normalized) word
     * exists as reviewed/accepted in other languages.
     */
    List<Language> detectSourceLanguages(String word, Language songLanguage);
}
