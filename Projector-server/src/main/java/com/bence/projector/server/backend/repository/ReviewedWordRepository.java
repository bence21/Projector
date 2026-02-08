package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ReviewedWordRepository extends CrudRepository<ReviewedWord, Long> {

    ReviewedWord findByLanguageAndWord(Language language, String word);

    List<ReviewedWord> findAllByLanguageAndStatus(Language language, ReviewedWordStatus status);

    List<ReviewedWord> findAllByLanguage(Language language);

    List<ReviewedWord> findByNormalizedWordAndLanguageNot(String normalizedWord, Language language);

    ReviewedWord findOneByUuid(String uuid);
}
