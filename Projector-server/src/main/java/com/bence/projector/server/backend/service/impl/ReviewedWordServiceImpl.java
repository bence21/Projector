package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.ReviewedWordRepository;
import com.bence.projector.server.backend.service.ReviewedWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    @Transactional
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
    @Transactional
    public List<ReviewedWord> saveBulkNewWords(List<String> words, Language language, User reviewedBy) {
        if (words == null || words.isEmpty() || language == null) {
            return new ArrayList<>();
        }

        List<ReviewedWord> reviewedWords = new ArrayList<>();
        Date reviewedDate = new Date();

        for (String word : words) {
            if (word == null || word.isEmpty()) {
                continue;
            }

            ReviewedWord reviewedWord = new ReviewedWord();
            reviewedWord.setWord(word); // This automatically sets normalizedWord via setWord()
            reviewedWord.setLanguage(language);
            reviewedWord.setStatus(ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC);
            reviewedWord.setReviewedBy(reviewedBy);
            reviewedWord.setReviewedDate(reviewedDate);
            reviewedWords.add(reviewedWord);
        }

        if (reviewedWords.isEmpty()) {
            return new ArrayList<>();
        }

        return (List<ReviewedWord>) reviewedWordRepository.saveAll(reviewedWords);
    }

    @Override
    public void deleteReview(String uuid) {
        ReviewedWord reviewedWord = findOneByUuid(uuid);
        if (reviewedWord != null) {
            reviewedWordRepository.delete(reviewedWord);
        }
    }
}
