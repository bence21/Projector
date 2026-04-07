package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Bible;
import com.bence.projector.server.backend.model.BibleVerse;
import com.bence.projector.server.backend.model.Book;
import com.bence.projector.server.backend.model.Chapter;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import com.bence.projector.server.backend.service.BibleService;
import com.bence.projector.server.backend.service.ReviewedWordService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bence.projector.server.utils.SetLanguages.getWordsFromText;

public class AutoAcceptWordsFromBibleUtil {

    public record AutoAcceptResult(int biblesProcessed) {
    }

    public static AutoAcceptResult autoAcceptWordsFromBible(
            ReviewedWordService reviewedWordService,
            BibleService bibleService,
            String bibleUuid,
            int minOccurrences) {

        Bible bible = validateAndGetBible(bibleService, bibleUuid);
        Language language = bible.getLanguage();
        if (language == null) {
            throw new IllegalArgumentException("Bible has no language: " + bibleUuid);
        }
        ExistingReviewedWords existing = getExistingReviewedWords(reviewedWordService, language);
        Map<String, Integer> wordsToSave = collectWordsFromBibles(Collections.singletonList(bible), existing, minOccurrences);
        saveReviewedWords(reviewedWordService, language, wordsToSave);
        return new AutoAcceptResult(1);
    }

    private static Bible validateAndGetBible(BibleService bibleService, String bibleUuid) {
        Bible bible = bibleService.findOneByUuid(bibleUuid);
        if (bible == null) {
            throw new IllegalArgumentException("Bible not found with UUID: " + bibleUuid);
        }
        return bible;
    }

    private record ExistingReviewedWords(Set<String> exactWords, Set<String> lowerCasedWords) {
    }

    private static ExistingReviewedWords getExistingReviewedWords(ReviewedWordService reviewedWordService, Language language) {
        List<ReviewedWord> all = reviewedWordService.findAllByLanguage(language);
        Set<String> exact = all.stream()
                .map(ReviewedWord::getWord)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> normalized = all.stream()
                .map(ReviewedWord::getWord)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return new ExistingReviewedWords(exact, normalized);
    }

    private static Map<String, Integer> collectWordsFromBibles(
            List<Bible> bibles,
            ExistingReviewedWords existing,
            int minOccurrences) {

        Map<String, Integer> wordCounts = new HashMap<>();
        for (Bible bible : bibles) {
            collectWordsFromBible(bible, existing, wordCounts);
        }
        return filterWordsByQuality(wordCounts, minOccurrences);
    }

    private static void collectWordsFromBible(
            Bible bible,
            ExistingReviewedWords existing,
            Map<String, Integer> wordCounts) {

        if (bible == null || bible.getBooks() == null) {
            return;
        }
        for (Book book : bible.getBooks()) {
            if (book == null || book.getChapters() == null) {
                continue;
            }
            for (Chapter chapter : book.getChapters()) {
                if (chapter == null || chapter.getVerses() == null) {
                    continue;
                }
                for (BibleVerse verse : chapter.getVerses()) {
                    collectWordsFromVerse(verse, existing, wordCounts);
                }
            }
        }
    }

    private static void collectWordsFromVerse(
            BibleVerse verse,
            ExistingReviewedWords existing,
            Map<String, Integer> wordCounts) {

        if (verse == null || verse.getText() == null || verse.getText().isEmpty()) {
            return;
        }
        for (String word : getWordsFromText(verse.getText())) {
            if (word.isEmpty() || existing.exactWords().contains(word)) {
                continue;
            }
            String lowerCase = word.toLowerCase();
            if (existing.lowerCasedWords().contains(lowerCase)) {
                continue;
            }
            wordCounts.merge(word, 1, Integer::sum);
        }
    }

    private static Map<String, Integer> filterWordsByQuality(Map<String, Integer> wordCounts, int minOccurrences) {
        if (wordCounts == null) {
            return new HashMap<>();
        }
        if (minOccurrences <= 0) {
            return wordCounts;
        }
        Map<String, Integer> filtered = new HashMap<>();
        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            if (entry.getValue() >= minOccurrences) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    private static void saveReviewedWords(
            ReviewedWordService reviewedWordService,
            Language language,
            Map<String, Integer> wordsToSave) {

        if (wordsToSave == null || wordsToSave.isEmpty()) {
            return;
        }
        List<String> wordsList = new ArrayList<>(new HashSet<>(wordsToSave.keySet()));
        reviewedWordService.saveBulkNewWords(wordsList, language, null, ReviewedWordStatus.AUTO_ACCEPTED_FROM_BIBLE);
    }
}
