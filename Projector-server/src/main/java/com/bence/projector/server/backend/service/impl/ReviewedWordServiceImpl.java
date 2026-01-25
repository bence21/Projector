package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.ReviewedWordRepository;
import com.bence.projector.server.backend.service.ReviewedWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.bence.projector.server.utils.StringUtils.normalizeAccents;

@Service
public class ReviewedWordServiceImpl extends BaseServiceImpl<ReviewedWord> implements ReviewedWordService {

    @Autowired
    private ReviewedWordRepository reviewedWordRepository;

    @Override
    public List<ReviewedWord> findAllByLanguageAndStatus(Language language, ReviewedWordStatus status) {
        return reviewedWordRepository.findAllByLanguageAndStatus(language, status);
    }

    @Override
    public List<ReviewedWord> findAllByLanguage(Language language) {
        return reviewedWordRepository.findAllByLanguage(language);
    }

    @Override
    public ReviewedWord findOneByUuid(String uuid) {
        return reviewedWordRepository.findOneByUuid(uuid);
    }

    @Override
    public ReviewedWord saveOrUpdate(ReviewedWord reviewedWord, User reviewedBy) {
        if (reviewedWord == null) {
            return null;
        }
        ReviewedWord existing = null;
        String word = reviewedWord.getWord();
        if (word != null && reviewedWord.getLanguage() != null) {
            // First, check for exact word match
            existing = reviewedWordRepository.findByLanguageAndWord(reviewedWord.getLanguage(), word);
        }
        
        ReviewedWord targetWord = existing != null ? existing : reviewedWord;
        targetWord.setReviewedBy(reviewedBy);
        targetWord.setReviewedDate(new Date());
        
        if (existing != null) {
            existing.setStatus(reviewedWord.getStatus());
            existing.setCategory(reviewedWord.getCategory());
            existing.setContextCategory(reviewedWord.getContextCategory());
            existing.setContextDescription(reviewedWord.getContextDescription());
            existing.setNotes(reviewedWord.getNotes());
        } else {
            if (reviewedWord.getNormalizedWord() == null && word != null) {
                reviewedWord.setNormalizedWord(normalizeAccents(word.toLowerCase()));
            }
        }
        return reviewedWordRepository.save(targetWord);
    }

    @Override
    public void deleteReview(String uuid) {
        ReviewedWord reviewedWord = findOneByUuid(uuid);
        if (reviewedWord != null) {
            reviewedWordRepository.delete(reviewedWord);
        }
    }
}
