package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.SuggestionDTO;
import com.bence.projector.server.api.assembler.SuggestionAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.NotificationByLanguage;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.Suggestion;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.SuggestionService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bence.projector.server.api.resources.SongResource.hasReviewerRoleForSong;
import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class SuggestionResource {
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private SuggestionService suggestionService;
    @Autowired
    private SuggestionAssembler suggestionAssembler;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private MailSenderService mailSenderService;
    @Autowired
    private UserService userService;
    @Autowired
    private SongService songService;
    @Autowired
    private SongRepository songRepository;

    @RequestMapping(value = "admin/api/suggestions", method = RequestMethod.GET)
    public List<SuggestionDTO> getSuggestions() {
        List<Suggestion> all = suggestionService.findAll();
        return suggestionAssembler.createDtosNoFetch(all);
    }

    private List<SuggestionDTO> getSuggestionDTOS(@PathVariable("languageId") String languageId) {
        Language language = languageService.findOneByUuid(languageId);
        List<Suggestion> allByLanguage = suggestionService.findAllByLanguageAndCustomFetch(language);
        return suggestionAssembler.createDtosNoFetch(allByLanguage);
    }

    @RequestMapping(value = "admin/api/suggestions/language/{languageId}", method = RequestMethod.GET)
    public List<SuggestionDTO> getSuggestionsByLanguage(@PathVariable("languageId") String languageId) {
        return getSuggestionDTOS(languageId);
    }

    @RequestMapping(value = "reviewer/api/suggestions/language/{languageId}", method = RequestMethod.GET)
    public List<SuggestionDTO> getSuggestionsByLanguageR(@PathVariable("languageId") String languageId) {
        return getSuggestionDTOS(languageId);
    }

    private SuggestionDTO getSuggestionDTO(@PathVariable String id) {
        Suggestion suggestion = suggestionService.findOneByUuid(id);
        return suggestionAssembler.createDto(suggestion);
    }

    @RequestMapping(value = "admin/api/suggestions/song/{songId}", method = RequestMethod.GET)
    public List<SuggestionDTO> getSuggestionsBySong(@PathVariable("songId") String songId) {
        return getSuggestionDTOBySong(songId);
    }

    @RequestMapping(value = "reviewer/api/suggestions/song/{songId}", method = RequestMethod.GET)
    public List<SuggestionDTO> getSuggestionsBySongR(@PathVariable("songId") String songId) {
        return getSuggestionDTOBySong(songId);
    }

    private List<SuggestionDTO> getSuggestionDTOBySong(@PathVariable String songId) {
        Song song = songService.findOneByUuid(songId);
        if (song == null) {
            return new ArrayList<>();
        }
        List<Suggestion> allBySong = suggestionService.findAllBySong(song);
        return suggestionAssembler.createDtoList(allBySong);
    }

    @RequestMapping(value = "admin/api/suggestion/{id}", method = RequestMethod.GET)
    public SuggestionDTO getSuggestion(@PathVariable final String id) {
        return getSuggestionDTO(id);
    }

    @RequestMapping(value = "reviewer/api/suggestion/{id}", method = RequestMethod.GET)
    public SuggestionDTO getSuggestionR(@PathVariable final String id) {
        return getSuggestionDTO(id);
    }

    @RequestMapping(value = "api/suggestion", method = RequestMethod.POST)
    public SuggestionDTO suggestion(@RequestBody final SuggestionDTO suggestionDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Suggestion model = suggestionAssembler.createModel(suggestionDTO);
        if (model != null) {
            Suggestion suggestion = suggestionService.save(model);
            Thread thread = new Thread(() -> sendEmail(suggestion));
            thread.start();
        }
        return suggestionAssembler.createDto(model);
    }

    private void sendEmail(Suggestion suggestion) {
        Song song = songService.findOneByUuid(suggestion.getSongUuid());
        if (song == null) {
            return;
        }
        List<User> reviewers = userService.findAllReviewersByLanguage(song.getLanguage());
        boolean was = false;
        for (User user : reviewers) {
            NotificationByLanguage notificationByLanguage = user.getNotificationByLanguage(song.getLanguage());
            if (notificationByLanguage != null && notificationByLanguage.isSuggestions()) {
                mailSenderService.sendEmailSuggestionToUser(suggestion, user);
                was = true;
            }
        }
        if (was) {
            mailSenderService.tryToSendAllPrevious();
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/admin/api/suggestion/{suggestionId}")
    public ResponseEntity<Object> updateSongByAdmin(Principal principal, @PathVariable final String suggestionId, @RequestBody final SuggestionDTO suggestionDTO, HttpServletRequest httpServletRequest) {
        return updateSuggestionByReviewer(principal, suggestionId, suggestionDTO, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reviewer/api/suggestion/{suggestionId}")
    public ResponseEntity<Object> updateSuggestionByReviewer(Principal principal, @PathVariable final String suggestionId, @RequestBody final SuggestionDTO suggestionDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                Suggestion suggestion = suggestionService.findOneByUuid(suggestionId);
                if (suggestion != null) {
                    Song song = songRepository.findOneByUuid(suggestion.getSongUuid());
                    if (hasReviewerRoleForSong(user, song)) {
                        Date modifiedDate = suggestion.getModifiedDate();
                        if ((modifiedDate != null && modifiedDate.compareTo(suggestionDTO.getModifiedDate()) != 0) || (modifiedDate == null && suggestionDTO.getModifiedDate() != null)) {
                            return new ResponseEntity<>("Already modified", HttpStatus.CONFLICT);
                        }
                        suggestionAssembler.updateModel(suggestion, suggestionDTO);
                        suggestion.setModifiedDate(new Date());
                        suggestion.setLastModifiedBy(user);
                        final Suggestion savedSuggestion = suggestionService.save(suggestion);
                        if (savedSuggestion != null) {
                            return new ResponseEntity<>(suggestionAssembler.createDto(savedSuggestion), HttpStatus.ACCEPTED);
                        }
                    }
                }
                return new ResponseEntity<>("Could not update", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }
}
