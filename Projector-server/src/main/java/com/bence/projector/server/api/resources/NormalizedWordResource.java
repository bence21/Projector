package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.ChangeWordDTO;
import com.bence.projector.common.dto.NormalizedWordBunchDTO;
import com.bence.projector.common.dto.WordBunchDTO;
import com.bence.projector.server.api.assembler.NormalizedWordBunchAssembler;
import com.bence.projector.server.api.assembler.ReviewedWordAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.ReviewedWordService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.utils.models.NormalizedWordBunch;
import com.bence.projector.server.utils.models.WordBunch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bence.projector.server.utils.SetLanguages.changeWordsInSongVerse;
import static com.bence.projector.server.utils.SetLanguages.getNormalizedWordBunches;
import static com.bence.projector.server.utils.SetLanguages.getSongWords;

@Controller
public class NormalizedWordResource {

    private final SongRepository songRepository;
    private final SongService songService;
    private final LanguageService languageService;
    private final NormalizedWordBunchAssembler normalizedWordBunchAssembler;
    private final ReviewedWordService reviewedWordService;
    private final ReviewedWordAssembler reviewedWordAssembler;

    @Autowired
    public NormalizedWordResource(
            SongRepository songRepository,
            SongService songService,
            LanguageService languageService,
            NormalizedWordBunchAssembler normalizedWordBunchAssembler,
            ReviewedWordService reviewedWordService,
            ReviewedWordAssembler reviewedWordAssembler
    ) {
        this.songRepository = songRepository;
        this.songService = songService;
        this.languageService = languageService;
        this.normalizedWordBunchAssembler = normalizedWordBunchAssembler;
        this.reviewedWordService = reviewedWordService;
        this.reviewedWordAssembler = reviewedWordAssembler;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}")
    public ResponseEntity<Object> getWordBunches(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return new ResponseEntity<>("Language not found", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(normalizedWordBunchAssembler.createDtoList(getAllWordBunchesForLanguage(language)), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}/banned")
    public ResponseEntity<Object> getBannedWordBunches(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        return getWordBunchesByStatus(languageId, ReviewedWordStatus.BANNED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}/reviewed-good")
    public ResponseEntity<Object> getReviewedGoodWordBunches(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        return getWordBunchesByStatus(languageId, ReviewedWordStatus.REVIEWED_GOOD);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}/context-specific")
    public ResponseEntity<Object> getContextSpecificWordBunches(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        return getWordBunchesByStatus(languageId, ReviewedWordStatus.CONTEXT_SPECIFIC);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}/accepted")
    public ResponseEntity<Object> getAcceptedWordBunches(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        return getWordBunchesByStatus(languageId, ReviewedWordStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}/rejected")
    public ResponseEntity<Object> getRejectedWordBunches(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        return getWordBunchesByStatus(languageId, ReviewedWordStatus.REJECTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}/unreviewed")
    public ResponseEntity<Object> getUnreviewedWordBunches(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return new ResponseEntity<>("Language not found", HttpStatus.BAD_REQUEST);
        }

        Set<String> reviewedWords = getReviewedWordsSet(language);
        List<NormalizedWordBunch> allBunches = getAllWordBunchesForLanguage(language);

        List<NormalizedWordBunch> unreviewed = new ArrayList<>();
        for (NormalizedWordBunch nwb : allBunches) {
            Set<String> bunchWords = new HashSet<>();
            for (com.bence.projector.server.utils.models.WordBunch wb : nwb.getWordBunches()) {
                bunchWords.add(wb.getNormalizedWord());
            }
            if (Collections.disjoint(bunchWords, reviewedWords)) {
                unreviewed.add(nwb);
            }
        }

        return new ResponseEntity<>(normalizedWordBunchAssembler.createDtoList(unreviewed), HttpStatus.ACCEPTED);
    }

    private ResponseEntity<Object> getWordBunchesByStatus(String languageId, ReviewedWordStatus status) {
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return new ResponseEntity<>("Language not found", HttpStatus.BAD_REQUEST);
        }

        List<ReviewedWord> reviewedWords = reviewedWordService.findAllByLanguageAndStatus(language, status);

        Set<String> normalizedWords = getNormalizedWordsSet(reviewedWords);
        if (normalizedWords.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.ACCEPTED);
        }

        List<NormalizedWordBunch> filtered = filterWordBunchesByNormalizedWords(language, normalizedWords);

        // Build maps for efficient lookup
        Map<String, NormalizedWordBunch> normalizedWordToBunchMap = createNormalizedWordToBunchMap(filtered);
        Set<String> exactWordsSet = createExactWordsSet(filtered);

        // Add reviewed words that don't have exact matches
        for (ReviewedWord reviewedWord : reviewedWords) {
            addReviewedWordToFiltered(filtered, reviewedWord, normalizedWordToBunchMap, exactWordsSet);
        }

        List<NormalizedWordBunchDTO> dtoList = normalizedWordBunchAssembler.createDtoList(filtered);

        Map<String, ReviewedWord> reviewedWordMap = createReviewedWordMap(reviewedWords);
        populateReviewedWordsInDtos(dtoList, reviewedWordMap);

        return new ResponseEntity<>(dtoList, HttpStatus.ACCEPTED);
    }

    private Set<String> getNormalizedWordsSet(List<ReviewedWord> reviewedWords) {
        return reviewedWords.stream()
                .map(ReviewedWord::getNormalizedWord)
                .collect(Collectors.toSet());
    }

    private Map<String, ReviewedWord> createReviewedWordMap(List<ReviewedWord> reviewedWords) {
        return reviewedWords.stream()
                .collect(Collectors.toMap(ReviewedWord::getWord, rw -> rw, (existing, replacement) -> existing));
    }

    private List<NormalizedWordBunch> filterWordBunchesByNormalizedWords(Language language, Set<String> normalizedWords) {
        List<NormalizedWordBunch> allBunches = getAllWordBunchesForLanguage(language);
        return allBunches.stream()
                .filter(nwb -> nwb.getWordBunches().stream()
                        .anyMatch(wb -> normalizedWords.contains(wb.getNormalizedWord())))
                .collect(Collectors.toList());
    }

    private void populateReviewedWordsInDtos(List<NormalizedWordBunchDTO> dtoList, Map<String, ReviewedWord> reviewedWordMap) {
        for (NormalizedWordBunchDTO nwbDto : dtoList) {
            populateReviewedWordInWordBunchList(nwbDto.getWordBunches(), reviewedWordMap);
            populateReviewedWordInWordBunchDto(nwbDto.getMaxBunch(), reviewedWordMap);
        }
    }

    private void populateReviewedWordInWordBunchList(List<WordBunchDTO> wordBunchDtos, Map<String, ReviewedWord> reviewedWordMap) {
        if (wordBunchDtos == null) {
            return;
        }
        for (WordBunchDTO wbDto : wordBunchDtos) {
            populateReviewedWordInWordBunchDto(wbDto, reviewedWordMap);
        }
    }

    private void populateReviewedWordInWordBunchDto(WordBunchDTO wbDto, Map<String, ReviewedWord> reviewedWordMap) {
        if (wbDto == null || wbDto.getWord() == null) {
            return;
        }
        ReviewedWord reviewedWord = reviewedWordMap.get(wbDto.getWord());
        if (reviewedWord != null) {
            wbDto.setReviewedWord(reviewedWordAssembler.createDto(reviewedWord));
        }
    }

    private List<NormalizedWordBunch> getAllWordBunchesForLanguage(Language language) {
        return getNormalizedWordBunches(
                songService.findAllByLanguage(language.getUuid()),
                languageService.findAll(),
                language
        );
    }

    private Set<String> getReviewedWordsSet(Language language) {
        return reviewedWordService.findAllByLanguage(language).stream()
                .map(ReviewedWord::getNormalizedWord)
                .collect(Collectors.toSet());
    }

    private Set<String> createExactWordsSet(List<NormalizedWordBunch> filtered) {
        Set<String> exactWordsSet = new HashSet<>();
        for (NormalizedWordBunch nwb : filtered) {
            for (WordBunch wb : nwb.getWordBunches()) {
                exactWordsSet.add(wb.getWord());
            }
        }
        return exactWordsSet;
    }

    private Map<String, NormalizedWordBunch> createNormalizedWordToBunchMap(List<NormalizedWordBunch> filtered) {
        Map<String, NormalizedWordBunch> map = new HashMap<>();
        for (NormalizedWordBunch nwb : filtered) {
            for (WordBunch wb : nwb.getWordBunches()) {
                String normalizedWord = wb.getNormalizedWord();
                // Only add if not already present (first match wins, same as original behavior)
                map.putIfAbsent(normalizedWord, nwb);
            }
        }
        return map;
    }

    private void addReviewedWordToFiltered(List<NormalizedWordBunch> filtered, ReviewedWord reviewedWord,
                                           Map<String, NormalizedWordBunch> normalizedWordToBunchMap,
                                           Set<String> exactWordsSet) {
        String word = reviewedWord.getWord();
        if (word == null) {
            return;
        }

        // Check if exact word match already exists
        if (exactWordsSet.contains(word)) {
            return;
        }

        String normalizedWord = reviewedWord.getNormalizedWord();
        if (normalizedWord == null) {
            return;
        }

        // Try to find existing NormalizedWordBunch by normalized word
        NormalizedWordBunch existingBunch = normalizedWordToBunchMap.get(normalizedWord);

        NormalizedWordBunch normalizedWordBunch;
        if (existingBunch != null) {
            normalizedWordBunch = existingBunch;
        } else {
            NormalizedWordBunch newNormalizedWordBunch = new NormalizedWordBunch();
            normalizedWordBunch = newNormalizedWordBunch;
            filtered.add(newNormalizedWordBunch);
            // Add to map for future lookups
            normalizedWordToBunchMap.put(normalizedWord, newNormalizedWordBunch);
        }
        WordBunch newWordBunch = new WordBunch();
        newWordBunch.setWord(word);
        newWordBunch.setCount(0);
        normalizedWordBunch.add(newWordBunch);
        normalizedWordBunch.calculateBest();
        // Update exact words set
        exactWordsSet.add(word);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/normalizedWordBunch/{languageId}/getSongs")
    public String getSongsContainingWord(
            @PathVariable final String languageId,
            @RequestParam("word") String word,
            Model model) {
        List<Song> containsInSongs = getContainsInSongs(languageId, word);
        model.addAttribute("songs", containsInSongs);
        return "queue";
    }

    private List<Song> getContainsInSongs(String languageId, String word) {
        Language language = languageService.findOneByUuid(languageId);
        List<Song> containsInSongs = new ArrayList<>();
        if (language == null) {
            return containsInSongs;
        }
        String regex = getWordMatchRegex(word);
        List<Song> songs = songRepository.findAllByLanguageAndVersesTextContains(language.getId(), regex);
        for (Song song : songs) {
            if (containsInSong(song, word)) {
                containsInSongs.add(song);
            }
        }
        return containsInSongs;
    }

    private static String getWordMatchRegex(String word) {
        return "\\b" + word + "\\b";
    }

    private static boolean containsInSong(Song song, String word) {
        Collection<String> songWords = getSongWords(song);
        for (String songWord : songWords) {
            if (songWord.equals(word)) {
                return true;
            }
        }
        return false;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/admin/api/normalizedWordBunch/changeAll/{languageId}")
    public ResponseEntity<Object> changeAll(
            @RequestBody final ChangeWordDTO changeWordDTO,
            HttpServletRequest httpServletRequest,
            @PathVariable final String languageId
    ) {
        String word = changeWordDTO.getWord();
        List<Song> songs = getContainsInSongs(languageId, word);

        long count = getWordCountInSongs(songs, word);
        if (count != changeWordDTO.getOccurrence()) {
            return new ResponseEntity<>("Found matches: " + count + " not equals " + changeWordDTO.getOccurrence(), HttpStatus.NOT_ACCEPTABLE);
        }
        String correction = changeWordDTO.getCorrection();
        Date modifiedDate = new Date();
        for (Song song : songs) {
            for (SongVerse songVerse : song.getVerses()) {
                changeWordsInSongVerse(songVerse, word, correction);
            }
            song.setModifiedDate(modifiedDate);
        }
        songService.saveAllAndRemoveCache(songs);
        return new ResponseEntity<>(changeWordDTO, HttpStatus.ACCEPTED);
    }

    private static long getWordCountInSongs(List<Song> songs, String word) {
        long count = 0;
        for (Song song : songs) {
            Collection<String> songWords = getSongWords(song);
            for (String songWord : songWords) {
                if (songWord.equals(word)) {
                    ++count;
                }
            }
        }
        return count;
    }
}
