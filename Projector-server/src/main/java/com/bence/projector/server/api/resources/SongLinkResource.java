package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.SongLinkDTO;
import com.bence.projector.server.api.assembler.SongLinkAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongLink;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongLinkService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.mailsending.MailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Date;
import java.util.List;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class SongLinkResource {
    private final StatisticsService statisticsService;
    private final SongLinkService songLinkService;
    private final SongLinkAssembler songLinkAssembler;
    private final UserService userService;
    private final SongService songService;
    private final LanguageService languageService;
    private final MailSenderService mailSenderService;
    private final SongRepository songRepository;

    @Autowired
    public SongLinkResource(StatisticsService statisticsService, SongLinkService songLinkService, SongLinkAssembler songLinkAssembler, UserService userService, SongService songService, LanguageService languageService, MailSenderService mailSenderService, SongRepository songRepository) {
        this.statisticsService = statisticsService;
        this.songLinkService = songLinkService;
        this.songLinkAssembler = songLinkAssembler;
        this.userService = userService;
        this.songService = songService;
        this.languageService = languageService;
        this.mailSenderService = mailSenderService;
        this.songRepository = songRepository;
    }

    @RequestMapping(value = "admin/api/songLinks", method = RequestMethod.GET)
    public List<SongLinkDTO> getSongLinks() {
        List<SongLink> all = songLinkService.findAllUnApplied();
        return songLinkAssembler.createDtoList(all);
    }

    @RequestMapping(value = "admin/api/songLinks/resolveApplied", method = RequestMethod.GET)
    public List<SongLinkDTO> resolveAppliedSongLinks() {
        return songLinkAssembler.createDtoList(songLinkService.resolveAppliedSongLinks());
    }

    @RequestMapping(value = "admin/api/songLinks/language/{languageId}", method = RequestMethod.GET)
    public List<SongLinkDTO> getSongLinksByLanguage(@PathVariable("languageId") String languageId) {
        Language language = languageService.findOneByUuid(languageId);
        List<SongLink> songLinks = songLinkService.findAllByLanguage(language);
        return songLinkAssembler.createDtoList(songLinks);
    }

    @RequestMapping(value = "admin/api/songLink/{id}", method = RequestMethod.GET)
    public SongLinkDTO getSongLink(@PathVariable final String id, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        SongLink songLink = songLinkService.findOneByUuid(id);
        return songLinkAssembler.createDto(songLink);
    }

    @RequestMapping(value = "api/songLink", method = RequestMethod.POST)
    public SongLinkDTO songLink(@RequestBody final SongLinkDTO songLinkDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        SongLink model = songLinkAssembler.createModel(songLinkDTO);
        if (model != null && !model.alreadyTheSameVersionGroup(songRepository)) {
            SongLink songLink = songLinkService.save(model);
            Thread thread = new Thread(() -> {
                mailSenderService.enqueueEmailVersionGroupLinkForAdmins(songLink);
                mailSenderService.tryToSendAllPrevious();
            });
            thread.start();
        }
        return songLinkAssembler.createDto(model);
    }

    @RequestMapping(value = "admin/api/songLink/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Object> updateSongLink(@PathVariable final String id,
                                                 @RequestBody final SongLinkDTO songLinkDTO,
                                                 Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                SongLink songLink = songLinkService.findOneByUuid(id);
                if (songLink != null) {
                    Date modifiedDate = songLink.getModifiedDate();
                    if (modifiedDate != null && modifiedDate.compareTo(songLinkDTO.getModifiedDate()) != 0) {
                        return new ResponseEntity<>("Already modified", HttpStatus.CONFLICT);
                    }
                    songLinkAssembler.updateModel(songLink, songLinkDTO);
                    songLink.setModifiedDate(new Date());
                    songLinkService.save(songLink);
                    return new ResponseEntity<>(songLinkAssembler.createDto(songLink), HttpStatus.ACCEPTED);
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    @RequestMapping(value = "user/api/songVersionGroup/{songId1}/{songId2}", method = RequestMethod.POST)
    public ResponseEntity<Object> userSongLink(@PathVariable("songId1") String songId1, @PathVariable("songId2") String songId2, HttpServletRequest httpServletRequest, Principal principal) {
        if (songId1.equals(songId2)) {
            return new ResponseEntity<>("Same song", HttpStatus.CONFLICT);
        }
        Song song1 = songService.findOneByUuid(songId1);
        Song song2 = songService.findOneByUuid(songId2);
        if (song1 == null || song2 == null || song1.isSameVersionGroup(song2)) {
            return new ResponseEntity<>("Null", HttpStatus.NO_CONTENT);
        }
        saveStatistics(httpServletRequest, statisticsService);
        User user = null;
        if (principal != null) {
            String email = principal.getName();
            user = userService.findByEmail(email);
        }
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        SongLink model = new SongLink();
        model.setApplied(false);
        model.setCreatedDate(new Date());
        model.setSong1(songRepository.findOneByUuid(songId1));
        model.setSong2(songRepository.findOneByUuid(songId2));
        model.setCreatedByEmail(user.getEmail());
        SongLink songLink = songLinkService.save(model);
        Thread thread = new Thread(() -> {
            mailSenderService.enqueueEmailVersionGroupLinkForAdmins(songLink);
            mailSenderService.tryToSendAllPrevious();
        });
        thread.start();
        return new ResponseEntity<>(songLinkAssembler.createDto(model), HttpStatus.ACCEPTED);
    }

}
