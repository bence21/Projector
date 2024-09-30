package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.BooleanResponse;
import com.bence.projector.common.dto.LoginSongDTO;
import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongTitleDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import com.bence.projector.server.api.assembler.SongAssembler;
import com.bence.projector.server.api.assembler.SongTitleAssembler;
import com.bence.projector.server.api.resources.util.UserPrincipalUtil;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.NotificationByLanguage;
import com.bence.projector.server.backend.model.Role;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.Suggestion;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongCollectionService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.SuggestionService;
import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.mailsending.MailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;
import static com.bence.projector.server.api.resources.UserPropertiesResource.getUserFromPrincipalAndUserService;
import static com.bence.projector.server.mailsending.MailSenderService.getDateFormatted2;
import static com.bence.projector.server.utils.SetLanguages.getLanguageWords;
import static com.bence.projector.server.utils.SetLanguages.setLanguagesForUnknown;
import static com.bence.projector.server.utils.SongUtil.getLastModifiedSong;

@RestController
public class SongResource {

    private final SongRepository songRepository;
    private final SongService songService;
    private final SongAssembler songAssembler;
    private final SongTitleAssembler songTitleAssembler;
    private final StatisticsService statisticsService;
    private final UserService userService;
    private final LanguageService languageService;
    private final MailSenderService mailSenderService;
    private final SongCollectionService songCollectionService;
    private final SuggestionService suggestionService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public SongResource(SongRepository songRepository,
                        SongService songService,
                        SongAssembler songAssembler,
                        SongTitleAssembler songTitleAssembler,
                        StatisticsService statisticsService,
                        UserService userService,
                        LanguageService languageService,
                        MailSenderService mailSenderService,
                        SuggestionService suggestionService,
                        SongCollectionService songCollectionService
    ) {
        this.songRepository = songRepository;
        this.songService = songService;
        this.songAssembler = songAssembler;
        this.songTitleAssembler = songTitleAssembler;
        this.statisticsService = statisticsService;
        this.userService = userService;
        this.languageService = languageService;
        this.mailSenderService = mailSenderService;
        this.songCollectionService = songCollectionService;
        this.suggestionService = suggestionService;
    }

    static boolean hasReviewerRoleForSong(User user, Song song) {
        if ((song == null) || (user == null)) {
            return false;
        }
        if (user.getRole().equals(Role.ROLE_ADMIN)) {
            return true;
        }
        if (user.getEmail().equals(song.getCreatedByEmail()) && userIsActivatedOrSongIsRecentlyCreated(user, song)) {
            return true;
        }
        return user.hasReviewLanguage(song.getLanguage());
    }

    private static boolean userIsActivatedOrSongIsRecentlyCreated(User user, Song song) {
        if (user.isActivated()) {
            return true;
        }
        if (song.isPublic()) {
            return false; // it was reviewed already. User can't change after that.
        }
        Date now = new Date();
        Date beforeOneWeak = new Date(now.getTime() - 1000 * 60 * 60 * 24 * 7);
        return song.getCreatedDate().after(beforeOneWeak);
    }

    private static void setVersionGroupAndDate(Song song, Date date, Song versionGroup) {
        song.setVersionGroup(versionGroup);
        song.setModifiedDate(date);
    }

    public static void createBackUpSong(Song song, SongService songService) {
        Song backUpSong = createBackUpSongWithoutSave(song);
        songService.save(backUpSong);
    }

    public static Song createBackUpSongWithoutSave(Song song) {
        Song backUpSong = new Song(song);
        backUpSong.setIsBackUp(true);
        song.setBackUp(backUpSong);
        return backUpSong;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs")
    public List<SongDTO> findAll(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> all = songService.findAll();
        return songAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs/language/{languageId}")
    public List<SongDTO> findAll(@PathVariable("languageId") String languageId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> all = songService.findAllByLanguage(languageId);
        return songAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs/language/{languageId}/modifiedDate/{lastModifiedDate}")
    public List<SongDTO> findAll(@PathVariable("languageId") String languageId,
                                 @PathVariable("lastModifiedDate") Long lastModifiedDate,
                                 HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> all = songService.findAllByLanguageAndModifiedDate(languageId, new Date(lastModifiedDate));
        return songAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songsAfterModifiedDate/{lastModifiedDate}")
    public List<SongDTO> getAllSongsAfterModifiedDate(@PathVariable Long lastModifiedDate, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> songs = songService.findAllAfterModifiedDate(new Date(lastModifiedDate));
        return songAssembler.createDtoList(songs);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songTitles")
    public List<SongTitleDTO> getAllSongTitles(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> songs = songService.findAll();
        return songTitleAssembler.createDtoList(songs);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songTitlesAfterModifiedDate/{lastModifiedDate}")
    public List<SongTitleDTO> getAllSongTitlesAfterModifiedDate(@PathVariable Long lastModifiedDate, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> songs = songService.findAllAfterModifiedDate(new Date(lastModifiedDate));
        return songTitleAssembler.createDtoList(songs);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songTitlesAfterModifiedDate/{lastModifiedDate}/language/{languageId}")
    public List<SongTitleDTO> getAllSongTitlesAfterModifiedDate(@PathVariable Long lastModifiedDate, HttpServletRequest httpServletRequest, @PathVariable("languageId") String languageId) {
        saveStatistics(httpServletRequest, statisticsService);
        List<Song> songs = songService.findAllByLanguageAndModifiedDate(languageId, new Date(lastModifiedDate));
        return songTitleAssembler.createDtoList(songs);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/api/songTitles/language/{languageId}")
    public ResponseEntity<Object> getAllSongTitlesByMyUploads(Principal principal, HttpServletRequest httpServletRequest, @PathVariable("languageId") String languageId) {
        saveStatistics(httpServletRequest, statisticsService);
        User user = UserPrincipalUtil.getUserFromPrincipalAndUserService(principal, userService);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Song> songs = songService.findAllByLanguageAndUser(languageId, user);
        return new ResponseEntity<>(songTitleAssembler.createDtoList(songs), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songTitlesInReview/language/{languageId}")
    public ResponseEntity<Object> getAllSongTitlesInReview(HttpServletRequest httpServletRequest, @PathVariable("languageId") String languageId, @RequestParam(required = false) Boolean myUploads, Principal principal) {
        saveStatistics(httpServletRequest, statisticsService);
        Language language = languageService.findOneByUuid(languageId);
        List<Song> all = songService.findAllInReviewByLanguage(language);
        if (myUploads != null && myUploads) {
            User user = UserPrincipalUtil.getUserFromPrincipalAndUserService(principal, userService);
            if (user != null) {
                all = songService.filterSongsByCreatedEmail(all, user.getEmail());
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        }
        return new ResponseEntity<>(songTitleAssembler.createDtoList(all), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songViews/language/{language}")
    public List<SongViewsDTO> getSongViewsByLanguage(HttpServletRequest httpServletRequest, @PathVariable("language") String languageId) {
        saveStatistics(httpServletRequest, statisticsService);
        List<Song> songs = songService.findAllByLanguageContainingViews(languageId);
        List<SongViewsDTO> songViewsDTOS = new ArrayList<>(songs.size());
        for (Song song : songs) {
            SongViewsDTO dto = new SongViewsDTO();
            dto.setUuid(song.getUuid());
            dto.setViews(song.getViews());
            songViewsDTOS.add(dto);
        }
        return songViewsDTOS;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songFavourites/language/{language}")
    public List<SongFavouritesDTO> getSongFavouritesByLanguage(HttpServletRequest httpServletRequest, @PathVariable("language") String languageId) {
        saveStatistics(httpServletRequest, statisticsService);
        List<Song> songs = songService.findAllByLanguageContainingFavourites(languageId);
        List<SongFavouritesDTO> songFavouritesDTOS = new ArrayList<>(songs.size());
        for (Song song : songs) {
            SongFavouritesDTO dto = new SongFavouritesDTO();
            dto.setUuid(song.getUuid());
            dto.setFavourites(song.getViews());
            songFavouritesDTOS.add(dto);
        }
        return songFavouritesDTOS;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/song")
    public SongDTO getSongByTitle(@RequestParam String title, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> songs = songService.findAllSongsLazy();
        for (Song song : songs) {
            if (song.getTitle().equals(title)) {
                return songAssembler.createDto(song);
            }
        }
        return new SongDTO();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/song/{songId}")
    public SongDTO getSong(@PathVariable final String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songRepository.findOneByUuid(songId);
        return songAssembler.createDto(song);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/api/song/delete/{songId}")
    public SongDTO deleteSong(@PathVariable final String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songRepository.findOneByUuid(songId);
        song.setDeleted(true);
        song.setModifiedDate(new Date());
        songService.save(song);
        return songAssembler.createDto(song);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/reviewer/api/song/delete/{songId}")
    public ResponseEntity<Object> deleteSongByReviewer(Principal principal, @PathVariable final String songId, HttpServletRequest httpServletRequest) {
        return deleteSongByUser(principal, songId, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/user/api/song/delete/{songId}")
    public ResponseEntity<Object> deleteSongByUserApi(Principal principal, @PathVariable final String songId, HttpServletRequest httpServletRequest) {
        return deleteSongByUser(principal, songId, httpServletRequest);
    }

    private ResponseEntity<Object> deleteSongByUser(Principal principal, String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                final Song song = songRepository.findOneByUuid(songId);
                if (!hasReviewerRoleForSong(user, song)) {
                    return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
                }
                song.setDeleted(true);
                song.setModifiedDate(new Date());
                song.setLastModifiedBy(user);
                songService.save(song);
                return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/api/song/erase/{songId}")
    public ResponseEntity<Object> eraseSong(@PathVariable final String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songRepository.findOneByUuid(songId);
        if (song == null) {
            return null;
        }
        if (song.isDeleted()) {
            songService.deleteByUuid(songId);
        }
        return new ResponseEntity<>("{}", HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/reviewer/api/song/erase/{songId}")
    public ResponseEntity<Object> eraseSongByReviewer(Principal principal, @PathVariable final String songId, HttpServletRequest httpServletRequest) {
        return eraseSongByUser(principal, songId, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/user/api/song/erase/{songId}")
    public ResponseEntity<Object> eraseSongByUserApi(Principal principal, @PathVariable final String songId, HttpServletRequest httpServletRequest) {
        return eraseSongByUser(principal, songId, httpServletRequest);
    }

    private ResponseEntity<Object> eraseSongByUser(Principal principal, String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                Song song = songRepository.findOneByUuid(songId);
                if (hasReviewerRoleForSong(user, song)) {
                    song.setReviewerErased(true);
                    song.setModifiedDate(new Date());
                    song.setLastModifiedBy(user);
                    final Song savedSong = songService.save(song);
                    if (savedSong != null) {
                        return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
                    }
                }
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/api/song/publish/{songId}")
    public SongDTO publishSong(@PathVariable final String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songRepository.findOneByUuid(songId);
        song.setDeleted(false);
        song.setModifiedDate(new Date());
        songService.save(song);
        return songAssembler.createDto(song);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/api/song")
    public ResponseEntity<Object> createSong(@RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest, Principal principal) {
        saveStatistics(httpServletRequest, statisticsService);
        User user = userService.findByEmail(principal.getName());
        if (user == null) {
            return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        final Song song = songAssembler.createModel(songDTO);
        song.setCreatedByEmail(user.getEmail());
        if (!user.isActivated()) {
            song.setDeleted(true);
            song.setUploaded(true);
        }
        final Date date = new Date();
        song.setCreatedDate(date);
        song.setModifiedDate(date);
        final Song savedSong = songService.save(song);
        if (savedSong != null) {
            Thread thread = new Thread(() -> sendEmail(song));
            thread.start();
            SongDTO dto = songAssembler.createDto(savedSong);
            updateUserWhenCreatedSong(user);
            return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void updateUserWhenCreatedSong(User user) {
        try {
            if (user.isHadUploadedSongs()) {
                return;
            }
            user.setHadUploadedSongs(true);
            user.setModifiedDate(new Date());
            userService.save(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmail(Song song) {
        Language language = song.getLanguage();
        List<User> reviewers = userService.findAllReviewersByLanguage(language);
        boolean was = false;
        for (User user : reviewers) {
            NotificationByLanguage notificationByLanguage = user.getNotificationByLanguage(language);
            if (notificationByLanguage != null && notificationByLanguage.isNewSongs()) {
                mailSenderService.sendEmailNewSongToUser(song, user);
                was = true;
            }
        }
        if (was) {
            mailSenderService.tryToSendAllPrevious();
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/api/song/upload")
    public ResponseEntity<Object> uploadSong(@RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Song song = songAssembler.createModel(songDTO);
        song.setOriginalId(songDTO.getUuid());
        song.setDeleted(true);
        song.setUploaded(true);
        final Song savedSong = songService.save(song);
        if (savedSong != null) {
            Thread thread = new Thread(() -> {
                List<Song> songs = songService.findAllSongsLazy();
                boolean deleted = false;
                for (Song song1 : songs) {
                    if (!savedSong.getUuid().equals(song.getUuid()) && songService.matches(savedSong, song1)) {
                        songService.deleteByUuid(savedSong.getUuid());
                        deleted = true;
                        break;
                    }
                }
                if (!deleted) {
                    sendEmail(savedSong);
                }
            });
            thread.start();
            SongDTO dto = songAssembler.createDto(savedSong);
            return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs/upload")
    public ResponseEntity<Object> uploadedSongs(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Song> all = songService.findAllByUploadedTrueAndDeletedTrueAndNotBackup();
        return new ResponseEntity<>(songTitleAssembler.createDtoList(all), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/admin/api/song/{songId}")
    public ResponseEntity<Object> updateSongByAdmin(Principal principal, @PathVariable final String songId, @RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                return updateSong(songId, songDTO, user);
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reviewer/api/song/{songId}")
    public ResponseEntity<Object> updateSongByReviewer(Principal principal, @PathVariable final String songId, @RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        return updateSongByUser(principal, songId, songDTO, httpServletRequest, false);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/user/api/song/{songId}")
    public ResponseEntity<Object> updateSongByUser(Principal principal, @PathVariable final String songId, @RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        return updateSongByUser(principal, songId, songDTO, httpServletRequest, false);
    }

    private ResponseEntity<Object> updateSongByUser(Principal principal, @PathVariable final String songId, @RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest, boolean changeLanguage) {
        saveStatistics(httpServletRequest, statisticsService);
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                Song song = songRepository.findOneByUuid(songId);
                if (hasReviewerRoleForSong(user, song)) {
                    if (!changeLanguage) {
                        songDTO.setLanguageDTO(null);
                    }
                    Date modifiedDate = song.getModifiedDate();
                    if (modifiedDate != null && modifiedDate.compareTo(songDTO.getModifiedDate()) != 0) {
                        return new ResponseEntity<>("Already modified", HttpStatus.CONFLICT);
                    }
                    songDTO.setModifiedDate(new Date());
                    createBackUpSong(song, songService);
                    song.setLastModifiedBy(user);
                    if (!user.isActivated() && song.isDeleted()) {
                        songDTO.setDeleted(true);
                    }
                    songAssembler.updateModel(song, songDTO);
                    song.setReviewerErased(false);
                    final Song savedSong = songService.save(song);
                    setSongSuggestionsReviewedForYoutubeUrl(savedSong, user);
                    if (savedSong != null) {
                        return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
                    }
                }
                return new ResponseEntity<>("Could not update", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    private void setSongSuggestionsReviewedForYoutubeUrl(Song song, User user) {
        try {
            if (song == null) {
                return;
            }
            String songYoutubeUrl = song.getYoutubeUrl();
            if (songYoutubeUrl == null) {
                return;
            }
            List<Suggestion> suggestions = suggestionService.findAllBySong(song);
            if (suggestions == null) {
                return;
            }
            for (Suggestion suggestion : suggestions) {
                String youtubeUrl = suggestion.getYoutubeUrl();
                if (!suggestion.isReviewed() && youtubeUrl != null) {
                    if (youtubeUrl.equals(songYoutubeUrl)) {
                        suggestion.setReviewed(true);
                        suggestion.setModifiedDate(new Date());
                        suggestion.setLastModifiedBy(user);
                        suggestionService.save(suggestion);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/reviewer/api/changeLanguageForSong/{songId}")
    public ResponseEntity<Object> changeLanguageByReviewer(Principal principal, @PathVariable final String songId, @RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        return changeLanguageByUser(principal, songId, songDTO, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/user/api/changeLanguageForSong/{songId}")
    public ResponseEntity<Object> changeLanguageByUserApi(Principal principal, @PathVariable final String songId, @RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        return changeLanguageByUser(principal, songId, songDTO, httpServletRequest);
    }

    private ResponseEntity<Object> changeLanguageByUser(Principal principal, String songId, SongDTO songDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<Object> responseEntity = updateSongByUser(principal, songId, songDTO, httpServletRequest, true);
        if (responseEntity.getStatusCode().equals(HttpStatus.ACCEPTED)) {
            Song song = songRepository.findOneByUuid(songId);
            Thread thread = new Thread(() -> sendEmail(song));
            thread.start();
        }
        return responseEntity;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/admin/api/changeLanguageForSong/{songId}")
    public ResponseEntity<Object> changeLanguageByAdmin(Principal principal, @PathVariable final String songId, @RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        return changeLanguageByReviewer(principal, songId, songDTO, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/removeDuplicates")
    public void removeDuplicates(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Iterable<Song> songs = songRepository.findAll();
        int exceptionCounter = 100;
        HashMap<String, Boolean> deletedMap = new HashMap<>();
        for (Song uploaded : songService.findAllByUploadedTrueAndDeletedTrueAndNotBackup()) {
            for (Song song : songs) {
                if (deletedMap.containsKey(song.getUuid())) {
                    continue;
                }
                if (!uploaded.getUuid().equals(song.getUuid()) && songService.matches(uploaded, song) && uploaded.getSongListElements().isEmpty() && !songHasCollection(uploaded)) {
                    if (songRepository.findOneByUuid(song.getUuid()) != null) {
                        try {
                            deletedMap.put(uploaded.getUuid(), true);
                            songService.deleteByUuid(uploaded.getUuid());
                        } catch (Exception e) {
                            e.printStackTrace();
                            --exceptionCounter;
                            if (exceptionCounter < 0) {
                                return;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/automaticallyReAssignLanguages")
    public void automaticallyReAssignLanguages(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        setLanguagesForUnknown(songRepository, languageService);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/printALanguageWord/{languageId}")
    public String printALanguageWord(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        return getLanguageWords_(languageId, true);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/printALanguageWordsSimple/{languageId}")
    public String printALanguageWordsSimple(HttpServletRequest httpServletRequest, @PathVariable final String languageId) {
        return getLanguageWords_(languageId, false);
    }

    private String getLanguageWords_(String languageId, boolean table) {
        Language language = languageService.findOneByUuid(languageId);
        if (language == null) {
            return "Language not found";
        }
        List<Language> languages = languageService.findAll();
        return getLanguageWords(songService.findAllByLanguage(language.getUuid()), languages, language, table);
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void checkEmptySongs_runEvery5Minute_() {
        List<Song> songsByVersesIsEmpty = getAllByVersesIsEmptyOrShort();
        int size = songsByVersesIsEmpty.size();
        if (size != 0) {
            System.out.println("Warning: found " + size + " empty songs!\t" + getDateFormatted2(new Date()));
            mailSenderService.sendEmailEmptySongs(songsByVersesIsEmpty);
        }
    }

    private List<Song> getAllByVersesIsEmptyOrShort() {
        List<Song> allByVersesIsEmpty = songRepository.findAllByVersesIsEmpty();
        List<Song> shortSongs = songRepository.findAllByVersesIsShort();
        allByVersesIsEmpty.addAll(shortSongs);
        return allByVersesIsEmpty;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/checkForEmptySongs")
    public ResponseEntity<Object> checkForEmptySongs() {
        List<Song> songsByVersesIsEmpty = getAllByVersesIsEmptyOrShort();
        int size = songsByVersesIsEmpty.size();
        if (size == 0) {
            printNotEmptySongsFound();
        } else {
            for (Song song : songsByVersesIsEmpty) {
                System.out.println(song.getSongLinkWithTitle());
            }
            System.out.println(size);
        }
        return new ResponseEntity<>(songAssembler.createDtoList(songsByVersesIsEmpty), HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/deleteEmptySongs")
    public String deleteEmptySongs() {
        List<Song> songsByVersesIsEmpty = songRepository.findAllByVersesIsEmpty();
        int size = songsByVersesIsEmpty.size();
        if (size == 0) {
            printNotEmptySongsFound();
        } else {
            for (Song song : songsByVersesIsEmpty) {
                if (song.isDeleted()) {
                    songService.deleteByUuid(song.getUuid());
                }
            }
        }
        String s = "Deleted: " + size;
        System.out.println(s);
        return s;
    }

    private static void printNotEmptySongsFound() {
        System.out.println("No empty songs were found.");
    }

    private boolean songHasCollection(Song song) {
        return !songCollectionService.findAllBySong(song).isEmpty();
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/password/api/song/{songId}")
    public ResponseEntity<Object> updateSongByPassword(@PathVariable final String songId,
                                                       @RequestBody final LoginSongDTO loginSongDTO,
                                                       HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final User user = userService.findByEmail(loginSongDTO.getUsername());
        if (user != null) {
            if (getPasswordEncoder().matches(loginSongDTO.getPassword(), user.getPassword())) {
                return updateSong(songId, loginSongDTO.getSongDTO(), user);
            }
        }
        return null;
    }

    private ResponseEntity<Object> updateSong(String songId, SongDTO songDTO, User user) {
        Song song = songRepository.findOneByUuid(songId);
        if (song != null) {
            Date modifiedDate = song.getModifiedDate();
            if (modifiedDate != null && modifiedDate.compareTo(songDTO.getModifiedDate()) != 0) {
                return new ResponseEntity<>("Already modified", HttpStatus.CONFLICT);
            }
            songDTO.setModifiedDate(new Date());
            songAssembler.updateModel(song, songDTO);
        } else {
            song = songAssembler.createModel(songDTO);
        }
        song.setLastModifiedBy(user);
        song.setReviewerErased(null);
        final Song savedSong = songService.save(song);
        if (savedSong != null) {
            setSongSuggestionsReviewedForYoutubeUrl(savedSong, user);
            return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not update", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private PasswordEncoder getPasswordEncoder() {
        if (passwordEncoder == null) {
            passwordEncoder = new BCryptPasswordEncoder();
        }
        return passwordEncoder;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs/similar/song/{songId}")
    @Transactional
    public ResponseEntity<Object> similarSongs(@PathVariable("songId") String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Song song = songRepository.findOneByUuid(songId);
        if (song != null) {
            final List<Song> similar = songService.findAllSimilar(song);
            if (similar != null) {
                return new ResponseEntity<>(songAssembler.createDtoList(similar), HttpStatus.ACCEPTED);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/api/songs/similar/song")
    public ResponseEntity<Object> similarSongsByPost(@RequestBody final SongDTO songDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Song song = songAssembler.createModel(songDTO);
        final List<Song> similar = songService.findAllSimilar(song);
        if (similar != null) {
            return new ResponseEntity<>(songAssembler.createDtoList(similar), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/api/song/{songId}/incViews")
    public ResponseEntity<Object> incrementViews(@PathVariable("songId") String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Song song = songRepository.findOneByUuid(songId);
        if (song != null) {
            song.incrementViews();
            song.setLastIncrementViewDate(new Date());
            songService.save(song);
            return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/api/song/{songId}/incFavourites")
    public ResponseEntity<Object> incrementFavourites(@PathVariable("songId") String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Song song = songRepository.findOneByUuid(songId);
        if (song != null) {
            song.incrementFavourites();
            song.setLastIncrementFavouritesDate(new Date());
            songService.save(song);
            return new ResponseEntity<>(songAssembler.createDto(song), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.POST, value = "admin/api/songVersionGroup/{songId1}/{songId2}")
    public ResponseEntity<Object> mergeSongVersionGroup(@PathVariable("songId1") String songId1, @PathVariable("songId2") String songId2, HttpServletRequest httpServletRequest) {
        if (songId1.equals(songId2)) {
            return new ResponseEntity<>("Same song", HttpStatus.CONFLICT);
        }
        Date date = new Date();
        Song song1 = songRepository.findOneByUuid(songId1);
        Song song2 = songRepository.findOneByUuid(songId2);
        if (song1 == null || song2 == null) {
            return new ResponseEntity<>("Null", HttpStatus.NO_CONTENT);
        }
        saveStatistics(httpServletRequest, statisticsService);
        String song1VersionGroup = getUuidFromVersionGroupSong(song1);
        String song2VersionGroup = getUuidFromVersionGroupSong(song2);
        if (song1VersionGroup == null) {
            song1VersionGroup = song1.getUuid();
        }
        if (song2VersionGroup == null) {
            song2VersionGroup = song2.getUuid();
        }
        if (!song1VersionGroup.equals(song2VersionGroup)) {
            List<Song> allByVersionGroup1 = songService.findAllByVersionGroup(song1VersionGroup);
            List<Song> allByVersionGroup2 = songService.findAllByVersionGroup(song2VersionGroup);
            int size1 = allByVersionGroup1.size();
            int size2 = allByVersionGroup2.size();
            if (size1 == size2) {
                double sum1 = 0;
                for (Song song : allByVersionGroup1) {
                    sum1 += song.getModifiedDate().getTime();
                }
                double sum2 = 0;
                for (Song song : allByVersionGroup2) {
                    sum2 += song.getModifiedDate().getTime();
                }
                if (sum1 < sum2) {
                    ++size1;
                } else {
                    ++size2;
                }
            }
            if (size1 < size2) {
                for (Song song : allByVersionGroup1) {
                    setVersionGroupAndDate(song, date, songService.findOneByUuid(song2VersionGroup));
                }
                songService.saveAllByRepository(allByVersionGroup1);
            } else {
                for (Song song : allByVersionGroup2) {
                    setVersionGroupAndDate(song, date, songService.findOneByUuid(song1VersionGroup));
                }
                songService.saveAllByRepository(allByVersionGroup2);
            }
        }
        return new ResponseEntity<>("Merged", HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.POST, value = "admin/api/songVersionGroup/remove/{songId}")
    public ResponseEntity<Object> removeFromSongVersionGroup(@PathVariable("songId") String songId, HttpServletRequest httpServletRequest) {
        Song song = songRepository.findOneByUuid(songId);
        if (song == null) {
            return new ResponseEntity<>("Null", HttpStatus.NO_CONTENT);
        }
        saveStatistics(httpServletRequest, statisticsService);
        String songVersionGroup = getUuidFromVersionGroupSong(song);
        Date date = new Date();
        if (songVersionGroup != null) {
            setVersionGroupAndDate(song, date, null);
            songRepository.save(song);
        } else {
            List<Song> allByVersionGroup = songService.findAllByVersionGroup(song.getUuid());
            Song lastModifiedSong = getLastModifiedSong(allByVersionGroup);
            List<Song> modifiedSongs = new ArrayList<>();
            for (Song aSong : allByVersionGroup) {
                if (!aSong.equals(song) && !aSong.equals(lastModifiedSong)) {
                    setVersionGroupAndDate(aSong, date, lastModifiedSong);
                    modifiedSongs.add(aSong);
                }
            }
            setVersionGroupAndDate(lastModifiedSong, date, null);
            modifiedSongs.add(lastModifiedSong);
            songService.saveAllByRepository(modifiedSongs);
        }
        return new ResponseEntity<>("Removed", HttpStatus.ACCEPTED);
    }

    private String getUuidFromVersionGroupSong(Song song) {
        if (song == null) {
            return null;
        }
        Song versionGroup = song.getVersionGroup();
        if (versionGroup == null) {
            return null;
        }
        return versionGroup.getUuid();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songs/versionGroup/{id}")
    public List<SongDTO> getSongsByVersionGroup(@PathVariable("id") String id) {
        List<Song> allByVersionGroup = songService.findAllByVersionGroup(id);
        return songAssembler.createDtoList(allByVersionGroup);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songsYoutube")
    public List<SongTitleDTO> getSongsContainingYoutubeUrl() {
        List<Song> allContainingYoutubeUrl = songService.findAllContainingYoutubeUrl();
        return songTitleAssembler.createDtoList(allContainingYoutubeUrl);
    }

    @RequestMapping(method = RequestMethod.GET, value = "admin/api/songTitlesReviewed/user/{userId}")
    public List<SongTitleDTO> getSongTitlesReviewedByUser(@PathVariable String userId) {
        User user = userService.findOneByUuid(userId);
        List<Song> songs = songService.findAllReviewedByUser(user);
        return songTitleAssembler.createDtoList(songs);
    }

    @RequestMapping(method = RequestMethod.GET, value = "user/api/song/{uuid}/hasReviewerRoleForSong")
    public ResponseEntity<BooleanResponse> hasReviewerRoleForSongApi(@PathVariable("uuid") String uuid, Principal principal) {
        User user = getUserFromPrincipal(principal);
        BooleanResponse booleanResponse = new BooleanResponse();
        booleanResponse.setResponse(hasReviewerRoleForSong(user, songRepository.findOneByUuid(uuid)));
        return new ResponseEntity<>(booleanResponse, HttpStatus.ACCEPTED);
    }

    private User getUserFromPrincipal(Principal principal) {
        return getUserFromPrincipalAndUserService(principal, userService);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/songTitlesContainingYouTube")
    public List<SongTitleDTO> getAllSongTitlesContainingYouTube(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        return getSongsContainingYoutubeUrl();
    }
}
