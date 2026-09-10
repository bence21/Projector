package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.server.api.assembler.SongAssembler;
import com.bence.projector.server.api.assembler.SongTitleAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Role;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.SongLinkRepository;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongCollectionElementService;
import com.bence.projector.server.backend.service.SongCollectionService;
import com.bence.projector.server.backend.service.SongLinkService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.SongWordValidationService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.SuggestionService;
import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.mailsending.MailSenderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.Principal;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Business-logic tests for song create/update endpoints (admin, reviewer, user paths).
 */
public class SongResourceCreateUpdateTest {

    private static final String WEB_SONG_CREATE_HEADER = "X-Projector-Web-Song-Create";
    private static final String SONG_ID = "song-uuid-1";
    private static final String USER_EMAIL = "user@test.com";
    private static final String REVIEWER_EMAIL = "reviewer@test.com";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final long MODIFIED_MS = 1_700_000_000_000L;

    private SongRepository songRepository;
    private SongService songService;
    private SongAssembler songAssembler;
    private UserService userService;
    private SongResource songResource;

    @Before
    public void setUp() {
        songRepository = mock(SongRepository.class);
        songService = mock(SongService.class);
        songAssembler = mock(SongAssembler.class);
        userService = mock(UserService.class);
        SongTitleAssembler songTitleAssembler = mock(SongTitleAssembler.class);
        StatisticsService statisticsService = mock(StatisticsService.class);
        LanguageService languageService = mock(LanguageService.class);
        MailSenderService mailSenderService = mock(MailSenderService.class);
        SuggestionService suggestionService = mock(SuggestionService.class);
        SongCollectionService songCollectionService = mock(SongCollectionService.class);
        SongLinkRepository songLinkRepository = mock(SongLinkRepository.class);
        SongLinkService songLinkService = mock(SongLinkService.class);
        SongCollectionElementService songCollectionElementService = mock(SongCollectionElementService.class);
        SongWordValidationService songWordValidationService = mock(SongWordValidationService.class);

        songResource = new SongResource(
                songRepository,
                songService,
                songAssembler,
                songTitleAssembler,
                statisticsService,
                userService,
                languageService,
                mailSenderService,
                suggestionService,
                songCollectionService,
                songLinkRepository,
                songLinkService,
                songCollectionElementService,
                songWordValidationService
        );

        doAnswer(inv -> inv.getArgument(0)).when(songService).save(any(Song.class));
        doAnswer(inv -> inv.getArgument(0)).when(userService).save(any(User.class));
        when(userService.findAllReviewersByLanguage(any())).thenReturn(Collections.emptyList());
        when(suggestionService.findAllBySong(any(Song.class))).thenReturn(Collections.emptyList());
    }

    // --- Create ---

    @Test
    public void createSong_web_activatedUser_doesNotMarkReviewQueue() {
        User user = user(USER_EMAIL, Role.ROLE_USER, true);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(user);
        Song song = new Song();
        when(songAssembler.createModel(any(SongDTO.class))).thenReturn(song);
        when(songAssembler.createDto(any(Song.class))).thenReturn(new SongDTO());

        ResponseEntity<Object> response = songResource.createSong(
                createSongDto(SONG_ID, "New Song"),
                webCreateRequest(),
                principal(USER_EMAIL));
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        verify(songService).save(captor.capture());
        Song saved = captor.getValue();
        assertFalse(saved.isDeleted() && Boolean.TRUE.equals(saved.getUploaded()));
        assertEquals(USER_EMAIL, saved.getCreatedByEmail());
    }

    @Test
    public void createSong_web_unactivatedUser_marksReviewQueue() {
        User user = user(USER_EMAIL, Role.ROLE_USER, false);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(user);
        Song song = new Song();
        when(songAssembler.createModel(any(SongDTO.class))).thenReturn(song);
        when(songAssembler.createDto(any(Song.class))).thenReturn(new SongDTO());

        ResponseEntity<Object> response = songResource.createSong(
                createSongDto(SONG_ID, "New Song"),
                webCreateRequest(),
                principal(USER_EMAIL));
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        verify(songService).save(captor.capture());
        assertTrue(captor.getValue().isDeleted());
        assertEquals(Boolean.TRUE, captor.getValue().getUploaded());
    }

    @Test
    public void createSong_native_alwaysMarksReviewQueue() {
        User user = user(USER_EMAIL, Role.ROLE_USER, true);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(user);
        Song song = new Song();
        when(songAssembler.createModel(any(SongDTO.class))).thenReturn(song);
        when(songAssembler.createDto(any(Song.class))).thenReturn(new SongDTO());

        ResponseEntity<Object> response = songResource.createSong(
                createSongDto(SONG_ID, "Native Song"),
                new MockHttpServletRequest(),
                principal(USER_EMAIL));
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        verify(songService).save(captor.capture());
        assertTrue(captor.getValue().isDeleted());
        assertEquals(Boolean.TRUE, captor.getValue().getUploaded());
    }

    @Test
    public void createSong_adminPrincipal_setsCreatedByEmail() {
        User admin = user(ADMIN_EMAIL, Role.ROLE_ADMIN, true);
        when(userService.findByEmail(ADMIN_EMAIL)).thenReturn(admin);
        Song song = new Song();
        when(songAssembler.createModel(any(SongDTO.class))).thenReturn(song);
        when(songAssembler.createDto(any(Song.class))).thenReturn(new SongDTO());

        songResource.createSong(createSongDto(SONG_ID, "Admin Song"), webCreateRequest(), principal(ADMIN_EMAIL));

        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        verify(songService).save(captor.capture());
        assertEquals(ADMIN_EMAIL, captor.getValue().getCreatedByEmail());
    }

    @Test
    public void createSong_reviewerPrincipal_setsCreatedByEmail() {
        User reviewer = user(REVIEWER_EMAIL, Role.ROLE_REVIEWER, true);
        when(userService.findByEmail(REVIEWER_EMAIL)).thenReturn(reviewer);
        Song song = new Song();
        when(songAssembler.createModel(any(SongDTO.class))).thenReturn(song);
        when(songAssembler.createDto(any(Song.class))).thenReturn(new SongDTO());

        songResource.createSong(createSongDto(SONG_ID, "Reviewer Song"), webCreateRequest(), principal(REVIEWER_EMAIL));

        ArgumentCaptor<Song> captor = ArgumentCaptor.forClass(Song.class);
        verify(songService).save(captor.capture());
        assertEquals(REVIEWER_EMAIL, captor.getValue().getCreatedByEmail());
    }

    @Test
    public void createSong_unknownUser_returns500() {
        when(userService.findByEmail(USER_EMAIL)).thenReturn(null);

        ResponseEntity<Object> response = songResource.createSong(
                createSongDto(SONG_ID, "Song"),
                new MockHttpServletRequest(),
                principal(USER_EMAIL));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(songService, never()).save(any(Song.class));
    }

    // --- Update admin ---

    @Test
    public void updateSongByAdmin_success() {
        User admin = user(ADMIN_EMAIL, Role.ROLE_ADMIN, true);
        Song song = songInDb(SONG_ID, language(1L), "other@test.com", new Date(MODIFIED_MS));
        when(userService.findByEmail(ADMIN_EMAIL)).thenReturn(admin);
        when(songRepository.findOneByUuid(SONG_ID)).thenReturn(song);
        when(songAssembler.updateModel(eq(song), any(SongDTO.class))).thenReturn(song);
        when(songAssembler.createDto(song)).thenReturn(new SongDTO());

        ResponseEntity<Object> response = songResource.updateSongByAdmin(
                principal(ADMIN_EMAIL),
                SONG_ID,
                updateSongDto("Updated", MODIFIED_MS),
                new MockHttpServletRequest());
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        verify(songAssembler).updateModel(eq(song), any(SongDTO.class));
        verify(songService).save(song);
        assertFalse(song.isReviewerErased());
        verify(songService, never()).persistBackUpSnapshot(any());
    }

    @Test
    public void updateSongByAdmin_optimisticLockConflict() {
        User admin = user(ADMIN_EMAIL, Role.ROLE_ADMIN, true);
        Song song = songInDb(SONG_ID, language(1L), "other@test.com", new Date(MODIFIED_MS));
        when(userService.findByEmail(ADMIN_EMAIL)).thenReturn(admin);
        when(songRepository.findOneByUuid(SONG_ID)).thenReturn(song);

        ResponseEntity<Object> response = songResource.updateSongByAdmin(
                principal(ADMIN_EMAIL),
                SONG_ID,
                updateSongDto("Updated", MODIFIED_MS + 1),
                new MockHttpServletRequest());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(songService, never()).save(any(Song.class));
    }

    @Test
    public void updateSongByAdmin_keepsLanguageDto() {
        User admin = user(ADMIN_EMAIL, Role.ROLE_ADMIN, true);
        Song song = songInDb(SONG_ID, language(1L), "other@test.com", new Date(MODIFIED_MS));
        when(userService.findByEmail(ADMIN_EMAIL)).thenReturn(admin);
        when(songRepository.findOneByUuid(SONG_ID)).thenReturn(song);
        when(songAssembler.updateModel(eq(song), any(SongDTO.class))).thenReturn(song);
        when(songAssembler.createDto(song)).thenReturn(new SongDTO());

        SongDTO dto = updateSongDto("Updated", MODIFIED_MS);
        LanguageDTO languageDTO = new LanguageDTO();
        languageDTO.setUuid("lang-1");
        dto.setLanguageDTO(languageDTO);

        songResource.updateSongByAdmin(principal(ADMIN_EMAIL), SONG_ID, dto, new MockHttpServletRequest());

        ArgumentCaptor<SongDTO> dtoCaptor = ArgumentCaptor.forClass(SongDTO.class);
        verify(songAssembler).updateModel(eq(song), dtoCaptor.capture());
        assertNotNull(dtoCaptor.getValue().getLanguageDTO());
    }

    // --- Update reviewer / user ---

    @Test
    public void updateSongByReviewer_withReviewLanguage_success() {
        Language language = language(10L);
        User reviewer = user(REVIEWER_EMAIL, Role.ROLE_REVIEWER, true);
        reviewer.setReviewLanguages(Collections.singletonList(language));
        Song song = songInDb(SONG_ID, language, "uploader@test.com", new Date(MODIFIED_MS));
        when(userService.findByEmail(REVIEWER_EMAIL)).thenReturn(reviewer);
        when(songRepository.findOneByUuid(SONG_ID)).thenReturn(song);
        when(songAssembler.updateModel(eq(song), any(SongDTO.class))).thenReturn(song);
        when(songAssembler.createDto(song)).thenReturn(new SongDTO());

        SongDTO dto = updateSongDto("Reviewer edit", MODIFIED_MS);
        LanguageDTO languageDTO = new LanguageDTO();
        languageDTO.setUuid("lang-10");
        dto.setLanguageDTO(languageDTO);

        ResponseEntity<Object> response = songResource.updateSongByReviewer(
                principal(REVIEWER_EMAIL),
                SONG_ID,
                dto,
                new MockHttpServletRequest());
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        verify(songService).persistBackUpSnapshot(any(Song.class));
        verify(songService).save(song);
        assertFalse(song.isReviewerErased());

        ArgumentCaptor<SongDTO> dtoCaptor = ArgumentCaptor.forClass(SongDTO.class);
        verify(songAssembler).updateModel(eq(song), dtoCaptor.capture());
        assertNull(dtoCaptor.getValue().getLanguageDTO());
    }

    @Test
    public void updateSongByUser_ownerActivated_success() {
        Language language = language(1L);
        User owner = user(USER_EMAIL, Role.ROLE_USER, true);
        Song song = songInDb(SONG_ID, language, USER_EMAIL, new Date(MODIFIED_MS));
        when(userService.findByEmail(USER_EMAIL)).thenReturn(owner);
        when(songRepository.findOneByUuid(SONG_ID)).thenReturn(song);
        when(songAssembler.updateModel(eq(song), any(SongDTO.class))).thenReturn(song);
        when(songAssembler.createDto(song)).thenReturn(new SongDTO());

        ResponseEntity<Object> response = songResource.updateSongByUser(
                principal(USER_EMAIL),
                SONG_ID,
                updateSongDto("User edit", MODIFIED_MS),
                new MockHttpServletRequest());
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        verify(songService).persistBackUpSnapshot(any(Song.class));
        verify(songService).save(song);
    }

    @Test
    public void updateSongByUser_forbidden_returns500() {
        User user = user(USER_EMAIL, Role.ROLE_USER, true);
        user.setReviewLanguages(Collections.emptyList());
        Song song = songInDb(SONG_ID, language(1L), "other@test.com", new Date(MODIFIED_MS));
        when(userService.findByEmail(USER_EMAIL)).thenReturn(user);
        when(songRepository.findOneByUuid(SONG_ID)).thenReturn(song);

        ResponseEntity<Object> response = songResource.updateSongByUser(
                principal(USER_EMAIL),
                SONG_ID,
                updateSongDto("Nope", MODIFIED_MS),
                new MockHttpServletRequest());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Could not update", response.getBody());
        verify(songService, never()).save(any(Song.class));
    }

    @Test
    public void updateSongByUser_optimisticLockConflict() {
        User owner = user(USER_EMAIL, Role.ROLE_USER, true);
        Song song = songInDb(SONG_ID, language(1L), USER_EMAIL, new Date(MODIFIED_MS));
        when(userService.findByEmail(USER_EMAIL)).thenReturn(owner);
        when(songRepository.findOneByUuid(SONG_ID)).thenReturn(song);

        ResponseEntity<Object> response = songResource.updateSongByUser(
                principal(USER_EMAIL),
                SONG_ID,
                updateSongDto("Stale", MODIFIED_MS + 1),
                new MockHttpServletRequest());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(songService, never()).save(any(Song.class));
    }

    @Test
    public void updateSongByUserPath_parityWithReviewerPath() {
        Language language = language(5L);
        User reviewer = user(REVIEWER_EMAIL, Role.ROLE_REVIEWER, true);
        reviewer.setReviewLanguages(Collections.singletonList(language));
        Song song = songInDb(SONG_ID, language, "x@test.com", new Date(MODIFIED_MS));
        when(userService.findByEmail(REVIEWER_EMAIL)).thenReturn(reviewer);
        when(songRepository.findOneByUuid(SONG_ID)).thenReturn(song);
        when(songAssembler.updateModel(eq(song), any(SongDTO.class))).thenReturn(song);
        when(songAssembler.createDto(song)).thenReturn(new SongDTO());

        ResponseEntity<Object> response = songResource.updateSongByUser(
                principal(REVIEWER_EMAIL),
                SONG_ID,
                updateSongDto("Via user path", MODIFIED_MS),
                new MockHttpServletRequest());
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        verify(songService).persistBackUpSnapshot(any(Song.class));
        verify(songAssembler).updateModel(eq(song), any(SongDTO.class));
        verify(songService).save(song);
    }

    private static Principal principal(String email) {
        return () -> email;
    }

    private static MockHttpServletRequest webCreateRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(WEB_SONG_CREATE_HEADER, "true");
        return request;
    }

    @SuppressWarnings("SameParameterValue")
    private static SongDTO createSongDto(String uuid, String title) {
        SongDTO dto = new SongDTO();
        dto.setUuid(uuid);
        dto.setTitle(title);
        return dto;
    }

    private static SongDTO updateSongDto(String title, long modifiedDateMs) {
        SongDTO dto = new SongDTO();
        dto.setTitle(title);
        dto.setModifiedDate(new Date(modifiedDateMs));
        return dto;
    }

    private static User user(String email, Role role, boolean activated) {
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        user.setActivated(activated);
        return user;
    }

    private static Language language(long id) {
        Language language = new Language();
        language.setId(id);
        language.setUuid("lang-" + id);
        return language;
    }

    @SuppressWarnings("SameParameterValue")
    private static Song songInDb(String uuid, Language language, String createdByEmail, Date modifiedDate) {
        Song song = new Song();
        song.setUuid(uuid);
        song.setLanguage(language);
        song.setCreatedByEmail(createdByEmail);
        song.setModifiedDate(modifiedDate);
        song.setVerses(Collections.emptyList());
        song.setSongVerseOrderListItems(Collections.emptyList());
        return song;
    }
}
