package com.bence.projector.server.backend.model;

import javax.persistence.*;
import java.util.Date;

import static com.bence.projector.server.utils.StringUtils.normalizeAccents;

@Entity
@Table(
        indexes = {
                @Index(name = "uuid_index", columnList = "uuid", unique = true),
                @Index(name = "word_language_index", columnList = "word,language_id,normalized_word", unique = true),
                @Index(name = "language_status_index", columnList = "language_id,status"),
                @Index(name = "normalized_word_index", columnList = "normalized_word")
        }
)
@org.hibernate.annotations.DynamicUpdate
public class ReviewedWord extends AbstractModel {

    @Column(nullable = false, length = 500)
    private String word;

    /**
     * Accent-normalized version of the word.
     * This field stores the word after applying accent normalization (lowercase + accent stripping via {@link com.bence.projector.server.utils.StringUtils#normalizeAccents(String)}).
     * Used for matching and comparison purposes where accent-insensitive matching is needed.
     */
    @Column(name = "normalized_word", nullable = false, length = 500)
    private String normalizedWord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewedWordStatus status;

    @Column(length = 100)
    private String category;

    @Column(name = "context_category", length = 100)
    private String contextCategory;

    @Column(name = "context_description", length = 500)
    private String contextDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reviewedDate;

    @Column(length = 1000)
    private String notes;

    public ReviewedWord() {
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
        if (word != null) {
            this.normalizedWord = normalizeAccents(word.toLowerCase());
        }
    }

    public String getNormalizedWord() {
        return normalizedWord;
    }

    public void setNormalizedWord(String normalizedWord) {
        this.normalizedWord = normalizedWord;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public ReviewedWordStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewedWordStatus status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContextCategory() {
        return contextCategory;
    }

    public void setContextCategory(String contextCategory) {
        this.contextCategory = contextCategory;
    }

    public String getContextDescription() {
        return contextDescription;
    }

    public void setContextDescription(String contextDescription) {
        this.contextDescription = contextDescription;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Date getReviewedDate() {
        return reviewedDate == null ? null : (Date) reviewedDate.clone();
    }

    public void setReviewedDate(Date reviewedDate) {
        this.reviewedDate = reviewedDate == null ? null : (Date) reviewedDate.clone();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
