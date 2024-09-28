package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.ChangeWordDTO;
import com.bence.projector.server.api.assembler.NormalizedWordBunchAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.utils.models.NormalizedWordBunch;
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
import java.util.Date;
import java.util.List;

import static com.bence.projector.server.utils.SetLanguages.getNormalizedWordBunches;
import static com.bence.projector.server.utils.StringUtils.countMatches;

@Controller
public class NormalizedWordResource {

    private final SongRepository songRepository;
    private final SongService songService;
    private final LanguageService languageService;
    private final NormalizedWordBunchAssembler normalizedWordBunchAssembler;

    @Autowired
    public NormalizedWordResource(
            SongRepository songRepository,
            SongService songService,
            LanguageService languageService,
            NormalizedWordBunchAssembler normalizedWordBunchAssembler
    ) {
        this.songRepository = songRepository;
        this.songService = songService;
        this.languageService = languageService;
        this.normalizedWordBunchAssembler = normalizedWordBunchAssembler;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/normalizedWordBunches/{languageId}")
    public ResponseEntity<Object> getWordBunches(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return new ResponseEntity<>("Language not found", HttpStatus.BAD_REQUEST);
        }
        List<Language> languages = languageService.findAll();
        List<NormalizedWordBunch> normalizedWordBunches = getNormalizedWordBunches(songService.findAllByLanguage(language.getUuid()), languages, language);
        return new ResponseEntity<>(normalizedWordBunchAssembler.createDtoList(normalizedWordBunches), HttpStatus.ACCEPTED);
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
            if (containsInSong(song, regex)) {
                containsInSongs.add(song);
            }
        }
        return containsInSongs;
    }

    private static String getWordMatchRegex(String word) {
        return "\\b" + word + "\\b";
    }

    private static boolean containsInSong(Song song, String regex) {
        for (SongVerse songVerse : song.getVerses()) {
            if (countMatches(songVerse.getText(), regex) > 0) {
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

        String regex = getWordMatchRegex(word);
        long count = 0;
        for (Song song : songs) {
            for (SongVerse songVerse : song.getVerses()) {
                count += countMatches(songVerse.getText(), regex);
            }
        }
        if (count != changeWordDTO.getOccurrence()) {
            return new ResponseEntity<>("Found matches: " + count + " not equals " + changeWordDTO.getOccurrence(), HttpStatus.NOT_ACCEPTABLE);
        }
        String correction = changeWordDTO.getCorrection();
        Date modifiedDate = new Date();
        for (Song song : songs) {
            for (SongVerse songVerse : song.getVerses()) {
                songVerse.setText(songVerse.getText().replaceAll(regex, correction));
            }
            song.setModifiedDate(modifiedDate);
        }
        songService.saveAllAndRemoveCache(songs);
        return new ResponseEntity<>(changeWordDTO, HttpStatus.ACCEPTED);
    }
}
