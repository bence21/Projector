package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.ChangeWordDTO;
import com.bence.projector.common.dto.NormalizedWordBunchDTO;
import com.bence.projector.common.dto.NormalizedWordBunchRowDTO;
import com.bence.projector.common.dto.PageResponse;
import com.bence.projector.common.dto.WordBunchDTO;
import com.bence.projector.server.api.assembler.NormalizedWordBunchAssembler;
import com.bence.projector.server.api.assembler.ReviewedWordAssembler;
import com.bence.projector.server.api.assembler.SongTitleAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.NormalizedWordBunchCacheService;
import com.bence.projector.server.backend.service.ReviewedWordService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.utils.models.NormalizedWordBunch;
import com.bence.projector.server.utils.models.SongWord;
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
import static com.bence.projector.server.utils.UnreviewedWordFileUtil.filterUnreviewedBunches;

@Controller
public class NormalizedWordResource {

    private final SongRepository songRepository;
    private final SongService songService;
    private final LanguageService languageService;
    private final NormalizedWordBunchAssembler normalizedWordBunchAssembler;
    private final ReviewedWordService reviewedWordService;
    private final ReviewedWordAssembler reviewedWordAssembler;
    private final NormalizedWordBunchCacheService normalizedWordBunchCacheService;
    private final SongTitleAssembler songTitleAssembler;

    @Autowired
    public NormalizedWordResource(
            SongRepository songRepository,
            SongService songService,
            LanguageService languageService,
            NormalizedWordBunchAssembler normalizedWordBunchAssembler,
            ReviewedWordService reviewedWordService,
            ReviewedWordAssembler reviewedWordAssembler,
            NormalizedWordBunchCacheService normalizedWordBunchCacheService,
            SongTitleAssembler songTitleAssembler
    ) {
        this.songRepository = songRepository;
        this.songService = songService;
        this.languageService = languageService;
        this.normalizedWordBunchAssembler = normalizedWordBunchAssembler;
        this.reviewedWordService = reviewedWordService;
        this.reviewedWordAssembler = reviewedWordAssembler;
        this.normalizedWordBunchCacheService = normalizedWordBunchCacheService;
        this.songTitleAssembler = songTitleAssembler;
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

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}/auto-accepted-from-public")
    public ResponseEntity<Object> getAutoAcceptedFromPublicWordBunches(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        return getWordBunchesByStatus(languageId, ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}/not-sure")
    public ResponseEntity<Object> getNotSureWordBunches(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        return getWordBunchesByStatus(languageId, ReviewedWordStatus.NOT_SURE);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}/unreviewed")
    public ResponseEntity<Object> getUnreviewedWordBunches(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return new ResponseEntity<>("Language not found", HttpStatus.BAD_REQUEST);
        }

        Set<String> reviewedWords = getReviewedWordsSet(language);
        List<NormalizedWordBunch> allBunches = getAllWordBunchesForLanguage(language);
        List<NormalizedWordBunch> unreviewed = filterUnreviewedBunches(allBunches, reviewedWords);

        return new ResponseEntity<>(normalizedWordBunchAssembler.createDtoList(unreviewed), HttpStatus.ACCEPTED);
    }

    /**
     * Paginated endpoint for words spell checker. Returns a single page of flattened rows.
     * Query params: page (0-based), size, filter (all | problematic | banned | reviewed-good | context-specific | accepted | rejected | not-sure | unreviewed | auto-accepted-from-public).
     */
    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}/page")
    public ResponseEntity<Object> getWordBunchesPage(
            @PathVariable final String languageId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestParam(value = "filter", defaultValue = "all") String filter
    ) {
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return new ResponseEntity<>("Language not found", HttpStatus.BAD_REQUEST);
        }

        List<NormalizedWordBunch> bunches = getFilteredBunchList(languageId, filter);
        if (bunches == null) {
            return new ResponseEntity<>("Invalid filter", HttpStatus.BAD_REQUEST);
        }

        List<SpellCheckerRowData> rows = flattenToRows(bunches);
        if ("problematic".equalsIgnoreCase(filter)) {
            rows = rows.stream().filter(r -> r.wb.isProblematic()).toList();
        }

        long totalElements = rows.size();
        int from = page * size;
        if (from >= totalElements) {
            return new ResponseEntity<>(new PageResponse<>(new ArrayList<>(), totalElements), HttpStatus.ACCEPTED);
        }
        int to = (int) Math.min((long) (page + 1) * size, totalElements);
        List<NormalizedWordBunchRowDTO> content = new ArrayList<>();
        Map<String, ReviewedWord> reviewedWordMap = createReviewedWordMap(reviewedWordService.findAllByLanguage(language));
        for (int i = from; i < to; i++) {
            SpellCheckerRowData rowData = rows.get(i);
            content.add(buildRowDto(i + 1, rowData, reviewedWordMap));
        }

        return new ResponseEntity<>(new PageResponse<>(content, totalElements), HttpStatus.ACCEPTED);
    }

    /**
     * Returns flattened list of (NormalizedWordBunch, WordBunch) in display order.
     */
    private List<SpellCheckerRowData> flattenToRows(List<NormalizedWordBunch> bunches) {
        List<SpellCheckerRowData> rows = new ArrayList<>();
        for (NormalizedWordBunch nwb : bunches) {
            for (WordBunch wb : nwb.getWordBunches()) {
                rows.add(new SpellCheckerRowData(nwb, wb));
            }
        }
        return rows;
    }

    private NormalizedWordBunchRowDTO buildRowDto(int nr, SpellCheckerRowData rowData, Map<String, ReviewedWord> reviewedWordMap) {
        NormalizedWordBunch nwb = rowData.nwb;
        WordBunch wb = rowData.wb;
        NormalizedWordBunchRowDTO dto = new NormalizedWordBunchRowDTO();
        dto.setNr(nr);
        dto.setConfidencePercentage(nwb.getRatio());
        dto.setWord(wb.getWord());
        dto.setCount(wb.getCount());
        dto.setProblematic(wb.isProblematic());
        dto.setAllOccurrencesAutoCapitalized(wb.getCapCount() > 0 && wb.getCapCount() == wb.getCount());
        if (wb.isProblematic() && nwb.getBestWord() != null) {
            String source = wb.getWord();
            String target = nwb.getBestWord();
            if (source != null && target != null && source.length() != target.length()) {
                dto.setCorrectionLengthMismatch(true);
            }
            dto.setCorrection(applyCaseByReference(source, target));
        }
        List<Song> songs = wb.getSongs();
        if (songs != null && !songs.isEmpty()) {
            dto.setSong(songTitleAssembler.createDto(songs.get(0)));
        }
        ReviewedWord rw = reviewedWordMap.get(wb.getWord());
        if (rw != null) {
            dto.setReviewedWord(reviewedWordAssembler.createDto(rw));
        }
        return dto;
    }

    private static String applyCaseByReference(String source, String target) {
        if (source == null || target == null || source.length() != target.length()) {
            return target;
        }
        StringBuilder sb = new StringBuilder(target.length());
        for (int i = 0; i < source.length(); i++) {
            char sc = source.charAt(i);
            char tc = target.charAt(i);
            sb.append(Character.isUpperCase(sc) ? Character.toUpperCase(tc) : Character.toLowerCase(tc));
        }
        return sb.toString();
    }

    /**
     * Returns the list of NormalizedWordBunch for the given filter (same data as non-paginated endpoints).
     */
    private List<NormalizedWordBunch> getFilteredBunchList(String languageId, String filter) {
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return null;
        }
        if (filter == null) {
            filter = "all";
        }
        switch (filter.toLowerCase()) {
            case "all", "problematic":
                return getAllWordBunchesForLanguage(language);
            case "banned":
                return getBunchesByStatus(languageId, ReviewedWordStatus.BANNED);
            case "reviewed-good":
                return getBunchesByStatus(languageId, ReviewedWordStatus.REVIEWED_GOOD);
            case "context-specific":
                return getBunchesByStatus(languageId, ReviewedWordStatus.CONTEXT_SPECIFIC);
            case "accepted":
                return getBunchesByStatus(languageId, ReviewedWordStatus.ACCEPTED);
            case "rejected":
                return getBunchesByStatus(languageId, ReviewedWordStatus.REJECTED);
            case "not-sure":
                return getBunchesByStatus(languageId, ReviewedWordStatus.NOT_SURE);
            case "auto-accepted-from-public":
                return getBunchesByStatus(languageId, ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC);
            case "unreviewed": {
                Set<String> reviewedWords = getReviewedWordsSet(language);
                List<NormalizedWordBunch> allBunches = getAllWordBunchesForLanguage(language);
                return filterUnreviewedBunches(allBunches, reviewedWords);
            }
            default:
                return null;
        }
    }

    /**
     * Returns list of NormalizedWordBunch for the given status (same logic as getWordBunchesByStatus but returns list only).
     */
    private List<NormalizedWordBunch> getBunchesByStatus(String languageId, ReviewedWordStatus status) {
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return new ArrayList<>();
        }
        List<ReviewedWord> reviewedWords = reviewedWordService.findAllByLanguageAndStatus(language, status);
        Set<String> normalizedWords = getNormalizedWordsSet(reviewedWords);
        if (normalizedWords.isEmpty()) {
            return new ArrayList<>();
        }
        List<NormalizedWordBunch> filtered = filterWordBunchesByNormalizedWords(language, normalizedWords);
        Map<String, NormalizedWordBunch> normalizedWordToBunchMap = createNormalizedWordToBunchMap(filtered);
        Set<String> exactWordsSet = createExactWordsSet(filtered);
        for (ReviewedWord reviewedWord : reviewedWords) {
            addReviewedWordToFiltered(filtered, reviewedWord, normalizedWordToBunchMap, exactWordsSet);
        }
        return filtered;
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
        return normalizedWordBunchCacheService.getAllWordBunchesForLanguage(language);
    }

    private void clearCacheForLanguage(String languageUuid) {
        normalizedWordBunchCacheService.clearCacheForLanguage(languageUuid);
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
        Collection<SongWord> songWords = getSongWords(song);
        for (SongWord songWord : songWords) {
            if (songWord.getWord().equals(word)) {
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
        // Get language object early for cache operations
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return new ResponseEntity<>("Language not found", HttpStatus.BAD_REQUEST);
        }

        String word = changeWordDTO.getWord();
        List<Song> songs = getContainsInSongs(languageId, word);

        long count = getWordCountInSongs(songs, word);
        if (count != changeWordDTO.getOccurrence()) {
            return new ResponseEntity<>("Found matches: " + count + " not equals " + changeWordDTO.getOccurrence(), HttpStatus.NOT_ACCEPTABLE);
        }

        // Get cached word bunches and compute word bunches for songs BEFORE changes
        List<NormalizedWordBunch> cachedBunches = getAllWordBunchesForLanguage(language);
        List<NormalizedWordBunch> oldBunches = getNormalizedWordBunches(
                songs,
                languageService.findAll(),
                language
        );

        String correction = changeWordDTO.getCorrection();
        Date modifiedDate = new Date();
        for (Song song : songs) {
            for (SongVerse songVerse : song.getVerses()) {
                changeWordsInSongVerse(songVerse, word, correction);
            }
            song.setModifiedDate(modifiedDate);
        }
        songService.saveAllAndRemoveCache(songs);

        // Compute word bunches for songs AFTER changes
        List<NormalizedWordBunch> newBunches = getNormalizedWordBunches(
                songs,
                languageService.findAll(),
                language
        );

        // Merge old and new bunches into cached bunches
        List<NormalizedWordBunch> mergedBunches = mergeWordBunches(cachedBunches, oldBunches, newBunches);
        normalizedWordBunchCacheService.updateCacheForLanguage(languageId, mergedBunches);

        return new ResponseEntity<>(changeWordDTO, HttpStatus.ACCEPTED);
    }

    private static long getWordCountInSongs(List<Song> songs, String word) {
        long count = 0;
        for (Song song : songs) {
            Collection<SongWord> songWords = getSongWords(song);
            for (SongWord songWord : songWords) {
                if (songWord.getWord().equals(word)) {
                    ++count;
                }
            }
        }
        return count;
    }

    /**
     * Merges word bunches by subtracting old counts and adding new counts.
     * Updates the cached word bunches with changes from modified songs.
     */
    private List<NormalizedWordBunch> mergeWordBunches(
            List<NormalizedWordBunch> cached,
            List<NormalizedWordBunch> oldBunches,
            List<NormalizedWordBunch> newBunches) {

        // Create a deep copy of cached bunches to avoid modifying the original
        List<NormalizedWordBunch> merged = new ArrayList<>();
        Map<String, NormalizedWordBunch> normalizedWordMap = new HashMap<>();

        // Copy cached bunches and create lookup map by normalized word
        copyCachedBunchesAndCreateMap(cached, merged, normalizedWordMap);

        // Subtract counts from old bunches
        subtractWordBunches(normalizedWordMap, oldBunches);

        // Add counts from new bunches
        addWordBunches(merged, normalizedWordMap, newBunches);

        // Remove word bunches with zero or negative counts and recalculate
        cleanupAndRecalculate(merged);

        return merged;
    }

    private void copyCachedBunchesAndCreateMap(List<NormalizedWordBunch> cached,
                                               List<NormalizedWordBunch> merged,
                                               Map<String, NormalizedWordBunch> normalizedWordMap) {
        for (NormalizedWordBunch nwb : cached) {
            NormalizedWordBunch copy = copyNormalizedWordBunch(nwb);
            merged.add(copy);
            // Use the first word bunch's normalized word as the key
            if (!copy.getWordBunches().isEmpty()) {
                String normalizedWord = copy.getWordBunches().get(0).getNormalizedWord();
                normalizedWordMap.put(normalizedWord, copy);
            }
        }
    }

    private NormalizedWordBunch copyNormalizedWordBunch(NormalizedWordBunch original) {
        NormalizedWordBunch copy = new NormalizedWordBunch();
        for (WordBunch wb : original.getWordBunches()) {
            WordBunch wbCopy = new WordBunch();
            wbCopy.setWord(wb.getWord());
            wbCopy.setCount(wb.getCount());
            copy.add(wbCopy);
        }
        return copy;
    }

    private void subtractWordBunches(Map<String, NormalizedWordBunch> normalizedWordMap,
                                     List<NormalizedWordBunch> oldBunches) {
        for (NormalizedWordBunch oldNwb : oldBunches) {
            for (WordBunch oldWb : oldNwb.getWordBunches()) {
                String normalizedWord = oldWb.getNormalizedWord();
                NormalizedWordBunch targetNwb = normalizedWordMap.get(normalizedWord);
                if (targetNwb != null) {
                    // Find matching word bunch by exact word
                    WordBunch targetWb = findWordBunchByWord(targetNwb, oldWb.getWord());
                    if (targetWb != null) {
                        targetWb.setCount(targetWb.getCount() - oldWb.getCount());
                    }
                }
            }
        }
    }

    private void addWordBunches(List<NormalizedWordBunch> merged,
                                Map<String, NormalizedWordBunch> normalizedWordMap,
                                List<NormalizedWordBunch> newBunches) {
        for (NormalizedWordBunch newNwb : newBunches) {
            // Get normalized word from first word bunch (all should have same normalized word)
            if (newNwb.getWordBunches().isEmpty()) {
                continue;
            }
            String normalizedWord = newNwb.getWordBunches().get(0).getNormalizedWord();
            NormalizedWordBunch targetNwb = normalizedWordMap.get(normalizedWord);

            if (targetNwb != null) {
                // Add all word bunches from new normalized word bunch
                for (WordBunch newWb : newNwb.getWordBunches()) {
                    // Find or create matching word bunch by exact word
                    WordBunch targetWb = findWordBunchByWord(targetNwb, newWb.getWord());
                    if (targetWb != null) {
                        targetWb.setCount(targetWb.getCount() + newWb.getCount());
                    } else {
                        // Create new word bunch
                        WordBunch wbCopy = new WordBunch();
                        wbCopy.setWord(newWb.getWord());
                        wbCopy.setCount(newWb.getCount());
                        targetNwb.add(wbCopy);
                    }
                }
            } else {
                // Create new normalized word bunch and add all its word bunches
                NormalizedWordBunch newNwbCopy = copyNormalizedWordBunch(newNwb);
                merged.add(newNwbCopy);
                normalizedWordMap.put(normalizedWord, newNwbCopy);
            }
        }
    }

    private WordBunch findWordBunchByWord(NormalizedWordBunch nwb, String word) {
        for (WordBunch wb : nwb.getWordBunches()) {
            if (wb.getWord().equals(word)) {
                return wb;
            }
        }
        return null;
    }

    private void cleanupAndRecalculate(List<NormalizedWordBunch> merged) {
        List<NormalizedWordBunch> toRemove = new ArrayList<>();
        for (NormalizedWordBunch nwb : merged) {
            // Remove word bunches with zero or negative counts
            List<WordBunch> toRemoveWb = new ArrayList<>();
            for (WordBunch wb : nwb.getWordBunches()) {
                if (wb.getCount() <= 0) {
                    toRemoveWb.add(wb);
                }
            }
            nwb.getWordBunches().removeAll(toRemoveWb);

            // Remove normalized word bunches that have no word bunches left
            if (nwb.getWordBunches().isEmpty()) {
                toRemove.add(nwb);
            } else {
                // Recalculate best word and ratio
                nwb.calculateBest();
            }
        }
        merged.removeAll(toRemove);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/admin/api/normalizedWordBunches/{languageId}/clearCache")
    public ResponseEntity<Object> clearCache(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        clearCacheForLanguage(languageId);
        return new ResponseEntity<>("Cache cleared for language " + languageId, HttpStatus.ACCEPTED);
    }

    /**
     * Holder for one flattened row before converting to DTO.
     */
    private record SpellCheckerRowData(NormalizedWordBunch nwb, WordBunch wb) {
    }
}
