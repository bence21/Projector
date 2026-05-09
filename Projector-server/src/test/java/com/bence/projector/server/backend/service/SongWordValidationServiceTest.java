package com.bence.projector.server.backend.service;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.common.dto.SongWordValidationResult;
import com.bence.projector.common.dto.WordWithStatus;
import com.bence.projector.server.api.assembler.LanguageAssembler;
import com.bence.projector.server.backend.model.ForeignLanguageType;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SongWordValidationServiceTest {

    @Mock
    private NormalizedWordBunchCacheService normalizedWordBunchCacheService;
    @Mock
    private LanguageAssembler languageAssembler;
    @Mock
    private ReviewedWordService reviewedWordService;

    private SongWordValidationService service;
    private Language songLanguage;

    /**
     * {@link SongWordValidationOptions#DEFAULT} omits mixed-language metrics; use this when tests need them.
     */
    private static final SongWordValidationOptions WITH_MIXED_LANGUAGE = new SongWordValidationOptions(true, true);

    @Before
    public void setUp() {
        service = new SongWordValidationService(normalizedWordBunchCacheService, languageAssembler, reviewedWordService);
        songLanguage = createLanguage("Hungarian");

        when(normalizedWordBunchCacheService.getAllWordBunchesForLanguage(any(Language.class))).thenReturn(new ArrayList<>());
        when(reviewedWordService.detectSourceLanguages(any(String.class), any(Language.class))).thenReturn(new ArrayList<>());
        when(languageAssembler.createDto(any(Language.class))).thenAnswer(invocation -> {
            Language language = invocation.getArgument(0);
            LanguageDTO dto = new LanguageDTO();
            dto.setEnglishName(language.getEnglishName());
            dto.setNativeName(language.getNativeName());
            return dto;
        });
    }

    @Test
    public void validateWords_warnsWhenForeignWordCountIsAtLeastTen() {
        // Tokens must stay unique after verse word extraction (trailing digits are stripped).
        List<String> foreignWords = IntStream.rangeClosed(1, 10).mapToObj(i -> "w" + i + "x").collect(Collectors.toList());
        Song song = createSong(songLanguage, foreignWords);

        Map<String, ReviewedWord> reviewedWordMap = new HashMap<>();
        for (String foreignWord : foreignWords) {
            reviewedWordMap.put(foreignWord, createReviewedAcceptedWord(songLanguage, foreignWord, createLanguage("English"), ForeignLanguageType.FOREIGN));
        }
        when(normalizedWordBunchCacheService.getReviewedWordMapForLanguage(songLanguage)).thenReturn(reviewedWordMap);

        SongWordValidationResult result = service.validateWords(song, WITH_MIXED_LANGUAGE);

        Assert.assertTrue(result.isHasMixedLanguageWarning());
        Assert.assertEquals(10, result.getForeignWordCount());
        Assert.assertEquals(10, result.getTotalReviewedWordCount());
        Assert.assertTrue(result.getForeignLanguages().contains("English"));
    }

    @Test
    public void validateWords_doesNotWarnAtExactlyFifteenPercentWhenBelowTenForeignWords() {
        List<String> foreignWords = IntStream.rangeClosed(1, 9).mapToObj(i -> "w" + i + "x").toList();
        List<String> localWords = IntStream.rangeClosed(1, 51).mapToObj(i -> "l" + i + "x").toList();
        List<String> allWords = new ArrayList<>();
        allWords.addAll(foreignWords);
        allWords.addAll(localWords);
        Song song = createSong(songLanguage, allWords);

        Map<String, ReviewedWord> reviewedWordMap = new HashMap<>();
        for (String foreignWord : foreignWords) {
            reviewedWordMap.put(foreignWord, createReviewedAcceptedWord(songLanguage, foreignWord, createLanguage("English"), ForeignLanguageType.FOREIGN));
        }
        for (String localWord : localWords) {
            reviewedWordMap.put(localWord, createReviewedAcceptedWord(songLanguage, localWord, null, null));
        }
        when(normalizedWordBunchCacheService.getReviewedWordMapForLanguage(songLanguage)).thenReturn(reviewedWordMap);

        SongWordValidationResult result = service.validateWords(song, WITH_MIXED_LANGUAGE);

        Assert.assertFalse(result.isHasMixedLanguageWarning());
        Assert.assertEquals(9, result.getForeignWordCount());
        Assert.assertEquals(60, result.getTotalReviewedWordCount());
        Assert.assertEquals(0.15d, result.getForeignWordRatio(), 0.0001d);
    }

    @Test
    public void validateWords_doesNotCountBorrowedWordsAsForeign() {
        List<String> borrowedWords = IntStream.rangeClosed(1, 11).mapToObj(i -> "b" + i + "z").collect(Collectors.toList());
        Song song = createSong(songLanguage, borrowedWords);

        Map<String, ReviewedWord> reviewedWordMap = new HashMap<>();
        for (String word : borrowedWords) {
            reviewedWordMap.put(word, createReviewedAcceptedWord(songLanguage, word, createLanguage("English"), ForeignLanguageType.BORROWED));
        }
        when(normalizedWordBunchCacheService.getReviewedWordMapForLanguage(songLanguage)).thenReturn(reviewedWordMap);

        SongWordValidationResult result = service.validateWords(song, WITH_MIXED_LANGUAGE);

        Assert.assertFalse(result.isHasMixedLanguageWarning());
        Assert.assertEquals(0, result.getForeignWordCount());
    }

    @Test
    public void validateWords_countsUnreviewedWordWhenRecognizedInOtherLanguage() {
        Song song = createSong(songLanguage, List.of("shalom"));
        when(normalizedWordBunchCacheService.getReviewedWordMapForLanguage(songLanguage)).thenReturn(new HashMap<>());
        when(reviewedWordService.detectSourceLanguages("shalom", songLanguage)).thenReturn(List.of(createLanguage("Hebrew")));

        SongWordValidationResult result = service.validateWords(song, WITH_MIXED_LANGUAGE);

        Assert.assertEquals(1, result.getForeignWordCount());
        Assert.assertTrue(result.getForeignLanguages().contains("Hebrew"));
    }

    @Test
    public void validateWords_fastIssueScan_skipsSuggestionsButRunsMixedLanguageDetection() {
        Song song = createSong(songLanguage, List.of("shalom"));
        when(normalizedWordBunchCacheService.getReviewedWordMapForLanguage(songLanguage)).thenReturn(new HashMap<>());
        when(reviewedWordService.detectSourceLanguages("shalom", songLanguage)).thenReturn(List.of(createLanguage("Hebrew")));

        SongWordValidationResult result = service.validateWords(song, SongWordValidationOptions.FAST_ISSUE_SCAN);

        verify(reviewedWordService, times(1)).detectSourceLanguages("shalom", songLanguage);
        Assert.assertEquals(1, result.getForeignWordCount());
        Assert.assertTrue(result.getForeignLanguages().contains("Hebrew"));
        WordWithStatus w = result.getWordsWithStatus().stream().filter(ws -> "shalom".equals(ws.getWord())).findFirst().orElse(null);
        Assert.assertNotNull(w);
        Assert.assertTrue(w.getSuggestions() == null || w.getSuggestions().isEmpty());
    }

    private Song createSong(Language language, List<String> words) {
        Song song = new Song();
        song.setTitle("Mixed language test song");
        song.setLanguage(language);
        SongVerse verse = new SongVerse();
        verse.setText(String.join(" ", words));
        List<SongVerse> verses = new ArrayList<>();
        verses.add(verse);
        song.setVerses(verses);
        return song;
    }

    private Language createLanguage(String englishName) {
        Language language = new Language();
        language.setEnglishName(englishName);
        language.setNativeName(englishName);
        return language;
    }

    private ReviewedWord createReviewedAcceptedWord(Language language, String word, Language sourceLanguage, ForeignLanguageType foreignLanguageType) {
        ReviewedWord reviewedWord = new ReviewedWord();
        reviewedWord.setLanguage(language);
        reviewedWord.setWord(word);
        reviewedWord.setStatus(ReviewedWordStatus.ACCEPTED);
        reviewedWord.setSourceLanguage(sourceLanguage);
        reviewedWord.setForeignLanguageType(foreignLanguageType);
        return reviewedWord;
    }
}
