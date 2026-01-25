package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.ReviewedWordDTO;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReviewedWordAssembler implements GeneralAssembler<ReviewedWord, ReviewedWordDTO> {

    private final LanguageAssembler languageAssembler;

    @Autowired
    public ReviewedWordAssembler(LanguageAssembler languageAssembler) {
        this.languageAssembler = languageAssembler;
    }

    @Override
    public ReviewedWordDTO createDto(ReviewedWord reviewedWord) {
        if (reviewedWord == null) {
            return null;
        }
        ReviewedWordDTO dto = new ReviewedWordDTO();
        dto.setUuid(reviewedWord.getUuid());
        dto.setWord(reviewedWord.getWord());
        dto.setNormalizedWord(reviewedWord.getNormalizedWord());
        dto.setLanguage(languageAssembler.createDto(reviewedWord.getLanguage()));
        dto.setStatus(reviewedWord.getStatus() != null ? reviewedWord.getStatus().name() : null);
        dto.setCategory(reviewedWord.getCategory());
        dto.setContextCategory(reviewedWord.getContextCategory());
        dto.setContextDescription(reviewedWord.getContextDescription());
        if (reviewedWord.getReviewedBy() != null) {
            dto.setReviewedByEmail(reviewedWord.getReviewedBy().getEmail());
            String firstName = reviewedWord.getReviewedBy().getFirstName();
            String surname = reviewedWord.getReviewedBy().getSurname();
            if (firstName != null || surname != null) {
                dto.setReviewedByName((firstName != null ? firstName : "") + 
                    (firstName != null && surname != null ? " " : "") + 
                    (surname != null ? surname : ""));
            }
        }
        dto.setReviewedDate(reviewedWord.getReviewedDate());
        dto.setNotes(reviewedWord.getNotes());
        return dto;
    }

    @Override
    public ReviewedWord createModel(ReviewedWordDTO dto) {
        if (dto == null) {
            return null;
        }
        ReviewedWord reviewedWord = new ReviewedWord();
        return updateModel(reviewedWord, dto);
    }

    @Override
    public ReviewedWord updateModel(ReviewedWord reviewedWord, ReviewedWordDTO dto) {
        if (reviewedWord == null || dto == null) {
            return reviewedWord;
        }
        reviewedWord.setWord(dto.getWord());
        if (dto.getStatus() != null) {
            try {
                reviewedWord.setStatus(ReviewedWordStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                // Invalid status, keep existing
            }
        }
        reviewedWord.setCategory(dto.getCategory());
        reviewedWord.setContextCategory(dto.getContextCategory());
        reviewedWord.setContextDescription(dto.getContextDescription());
        reviewedWord.setNotes(dto.getNotes());
        return reviewedWord;
    }
}
