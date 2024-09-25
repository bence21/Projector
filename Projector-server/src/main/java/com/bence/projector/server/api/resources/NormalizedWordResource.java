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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

import static com.bence.projector.server.utils.SetLanguages.getNormalizedWordBunches;
import static com.bence.projector.server.utils.StringUtils.countMatches;

@RestController
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

    @RequestMapping(method = RequestMethod.POST, value = "/admin/api/normalizedWordBunch/changeAll/{languageId}")
    public ResponseEntity<Object> changeAll(
            @RequestBody final ChangeWordDTO changeWordDTO,
            HttpServletRequest httpServletRequest,
            @PathVariable final String languageId
    ) {
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return new ResponseEntity<>("Language not found", HttpStatus.BAD_REQUEST);
        }
        String word = changeWordDTO.getWord();
        String regex = "\\b" + word + "\\b";
        List<Song> songs = songRepository.findAllByLanguageAndVersesTextContains(language.getId(), regex);

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
