package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.server.api.assembler.LanguageAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;
import static com.bence.projector.server.api.resources.util.UserPrincipalUtil.getUserFromPrincipalAndUserService;

@RestController
public class LanguageResource {

    private final LanguageService languageService;
    private final LanguageAssembler languageAssembler;
    private final StatisticsService statisticsService;
    private final UserService userService;

    @Autowired
    public LanguageResource(LanguageService languageService, LanguageAssembler languageAssembler, StatisticsService statisticsService, UserService userService) {
        this.languageService = languageService;
        this.languageAssembler = languageAssembler;
        this.statisticsService = statisticsService;
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/languages")
    public List<LanguageDTO> findAll(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Language> all = languageService.findAll();
        languageService.sortBySize(all);
        return languageAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/languages/deleted")
    public List<LanguageDTO> findAllDeletedSongs(HttpServletRequest httpServletRequest) {
        final List<Language> all = languageService.findAllDeleted();
        languageService.sortBySize(all);
        return languageAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.POST, value = "user/api/language")
    public ResponseEntity<Object> create(@RequestBody LanguageDTO languageDTO, HttpServletRequest httpServletRequest, Principal principal) {
        saveStatistics(httpServletRequest, statisticsService);
        User user = getUserFromPrincipalAndUserService(principal, userService);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String englishName = languageDTO.getEnglishName();
        if (englishName == null || englishName.trim().isEmpty()) {
            return new ResponseEntity<>("englishName is required", HttpStatus.BAD_REQUEST);
        }
        Language existing = languageService.findActiveByEnglishName(englishName);
        if (existing != null) {
            return new ResponseEntity<>(languageAssembler.createDto(existing), HttpStatus.CONFLICT);
        }
        Language language = languageAssembler.createModel(languageDTO);
        language.setSongs(new ArrayList<>());
        language.setCreatedBy(user);
        final Language savedLanguage = languageService.save(language);
        if (savedLanguage != null) {
            return new ResponseEntity<>(languageAssembler.createDto(savedLanguage), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
