package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.common.dto.ReviewedWordDTO;
import com.bence.projector.server.api.assembler.LanguageAssembler;
import com.bence.projector.server.api.assembler.ReviewedWordAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.model.ReviewedWordStatus;
import com.bence.projector.server.backend.model.Role;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.ReviewedWordService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.utils.AutoAcceptWordsFromPublicSongsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

import static com.bence.projector.server.api.resources.util.UserPrincipalUtil.getUserFromPrincipalAndUserService;

@Controller
public class ReviewedWordResource {

    private final ReviewedWordService reviewedWordService;
    private final ReviewedWordAssembler reviewedWordAssembler;
    private final LanguageService languageService;
    private final LanguageAssembler languageAssembler;
    private final UserService userService;
    private final SongService songService;

    @Autowired
    public ReviewedWordResource(
            ReviewedWordService reviewedWordService,
            ReviewedWordAssembler reviewedWordAssembler,
            LanguageService languageService,
            LanguageAssembler languageAssembler,
            UserService userService,
            SongService songService
    ) {
        this.reviewedWordService = reviewedWordService;
        this.reviewedWordAssembler = reviewedWordAssembler;
        this.languageService = languageService;
        this.languageAssembler = languageAssembler;
        this.userService = userService;
        this.songService = songService;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/admin/api/reviewedWord/{languageId}")
    public ResponseEntity<Object> createOrUpdateReviewedWord(
            Principal principal,
            @PathVariable final String languageId,
            @RequestBody final ReviewedWordDTO reviewedWordDTO
    ) {
        ResponseEntity<Object> validationError = validateUserAndLanguage(principal, languageId);
        if (validationError != null) {
            return validationError;
        }
        User user = getUserFromPrincipalAndUserService(principal, userService);
        Language language = languageService.findOneByUuid(languageId);

        ReviewedWord reviewedWord = reviewedWordAssembler.createModel(reviewedWordDTO);
        if (reviewedWord == null) {
            return new ResponseEntity<>("Invalid data", HttpStatus.BAD_REQUEST);
        }

        reviewedWord.setLanguage(language);
        ReviewedWord saved = reviewedWordService.saveOrUpdate(reviewedWord, user);
        return new ResponseEntity<>(reviewedWordAssembler.createDto(saved), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/reviewedWord/{languageId}")
    public ResponseEntity<Object> getAllReviewedWords(
            Principal principal,
            @PathVariable final String languageId
    ) {
        ResponseEntity<Object> validationError = validateUserAndLanguage(principal, languageId);
        if (validationError != null) {
            return validationError;
        }
        Language language = languageService.findOneByUuid(languageId);

        List<ReviewedWord> reviewedWords = reviewedWordService.findAllByLanguage(language);
        return new ResponseEntity<>(reviewedWordAssembler.createDtoList(reviewedWords), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/reviewedWord/{languageId}/status/{status}")
    public ResponseEntity<Object> getReviewedWordsByStatus(
            Principal principal,
            @PathVariable final String languageId,
            @PathVariable final String status
    ) {
        ResponseEntity<Object> validationError = validateUserAndLanguage(principal, languageId);
        if (validationError != null) {
            return validationError;
        }
        Language language = languageService.findOneByUuid(languageId);

        ReviewedWordStatus wordStatus;
        try {
            wordStatus = ReviewedWordStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid status", HttpStatus.BAD_REQUEST);
        }

        List<ReviewedWord> reviewedWords = reviewedWordService.findAllByLanguageAndStatus(language, wordStatus);
        return new ResponseEntity<>(reviewedWordAssembler.createDtoList(reviewedWords), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/api/reviewedWord/{wordId}")
    public ResponseEntity<Object> deleteReviewedWord(
            Principal principal,
            @PathVariable final String wordId
    ) {
        User user = getUserFromPrincipalAndUserService(principal, userService);
        if (user == null) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        ReviewedWord reviewedWord = reviewedWordService.findOneByUuid(wordId);
        if (reviewedWord == null) {
            return new ResponseEntity<>("Reviewed word not found", HttpStatus.NOT_FOUND);
        }

        if (lacksReviewerPermission(user, reviewedWord.getLanguage())) {
            return new ResponseEntity<>("Forbidden - reviewer permission required", HttpStatus.FORBIDDEN);
        }

        reviewedWordService.deleteReview(wordId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/admin/api/reviewedWord/{languageId}/bulk")
    public ResponseEntity<Object> bulkUpdateReviewedWords(
            Principal principal,
            @PathVariable final String languageId,
            @RequestBody final List<ReviewedWordDTO> reviewedWordDTOs
    ) {
        ResponseEntity<Object> validationError = validateUserAndLanguage(principal, languageId);
        if (validationError != null) {
            return validationError;
        }
        User user = getUserFromPrincipalAndUserService(principal, userService);
        Language language = languageService.findOneByUuid(languageId);

        if (reviewedWordDTOs == null || reviewedWordDTOs.isEmpty()) {
            return new ResponseEntity<>("Empty list", HttpStatus.BAD_REQUEST);
        }

        List<ReviewedWord> savedWords = new java.util.ArrayList<>();
        for (ReviewedWordDTO dto : reviewedWordDTOs) {
            ReviewedWord reviewedWord = reviewedWordAssembler.createModel(dto);
            if (reviewedWord != null) {
                reviewedWord.setLanguage(language);
                ReviewedWord saved = reviewedWordService.saveOrUpdate(reviewedWord, user);
                savedWords.add(saved);
            }
        }

        return new ResponseEntity<>(reviewedWordAssembler.createDtoList(savedWords), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/reviewedWord/detect-language")
    public ResponseEntity<Object> detectSourceLanguages(
            Principal principal,
            @RequestParam("word") String word,
            @RequestParam("languageId") String languageId
    ) {
        ResponseEntity<Object> validationError = validateUserAndLanguage(principal, languageId);
        if (validationError != null) {
            return validationError;
        }
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return new ResponseEntity<>("Language not found", HttpStatus.BAD_REQUEST);
        }
        List<Language> detected = reviewedWordService.detectSourceLanguages(word, language);
        List<LanguageDTO> dtos = languageAssembler.createDtoList(detected);
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @RequestMapping(method = {RequestMethod.GET}, value = "/admin/api/reviewedWord/{languageId}/autoAcceptFromPublicSongs")
    public ResponseEntity<Object> autoAcceptWordsFromPublicSongs(
            Principal principal,
            @PathVariable final String languageId,
            @RequestParam(value = "minScore", required = false, defaultValue = "50") final long minScore,
            @RequestParam(value = "minOccurrences", required = false, defaultValue = "10") final int minOccurrences
    ) {
        ResponseEntity<Object> validationError = validateUserAndLanguage(principal, languageId);
        if (validationError != null) {
            return validationError;
        }

        try {
            AutoAcceptWordsFromPublicSongsUtil.AutoAcceptResult result = AutoAcceptWordsFromPublicSongsUtil
                    .autoAcceptWordsFromPublicSongs(
                            languageService,
                            reviewedWordService,
                            songService,
                            languageId,
                            minScore,
                            minOccurrences
                    );

            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("songsProcessed", result.songsProcessed());
            response.put("minScore", minScore);
            response.put("minOccurrences", minOccurrences);

            return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error processing request: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Object> validateUserAndLanguage(Principal principal, String languageId) {
        User user = getUserFromPrincipalAndUserService(principal, userService);
        if (user == null) {
            return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
        }

        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return new ResponseEntity<>("Language not found", HttpStatus.BAD_REQUEST);
        }

        if (lacksReviewerPermission(user, language)) {
            return new ResponseEntity<>("Forbidden - reviewer permission required", HttpStatus.FORBIDDEN);
        }

        return null;
    }

    private boolean lacksReviewerPermission(User user, Language language) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            return false;
        }
        return !user.hasReviewLanguage(language);
    }
}
