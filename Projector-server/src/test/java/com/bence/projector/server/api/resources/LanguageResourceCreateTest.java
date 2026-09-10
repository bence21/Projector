package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.server.api.assembler.LanguageAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.Principal;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LanguageResourceCreateTest {

    private static final String USER_EMAIL = "user@test.com";

    private LanguageService languageService;
    private LanguageAssembler languageAssembler;
    private UserService userService;
    private LanguageResource languageResource;

    @Before
    public void setUp() {
        languageService = mock(LanguageService.class);
        languageAssembler = mock(LanguageAssembler.class);
        userService = mock(UserService.class);
        StatisticsService statisticsService = mock(StatisticsService.class);
        languageResource = new LanguageResource(languageService, languageAssembler, statisticsService, userService);
    }

    @Test
    public void create_unauthenticated_returnsUnauthorized() {
        LanguageDTO dto = new LanguageDTO();
        dto.setEnglishName("Klingon");
        dto.setNativeName("tlhIngan Hol");

        ResponseEntity<Object> response = languageResource.create(dto, new MockHttpServletRequest(), null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(languageService, never()).save(any(Language.class));
    }

    @Test
    public void create_blankEnglishName_returnsBadRequest() {
        LanguageDTO dto = new LanguageDTO();
        dto.setEnglishName("   ");
        dto.setNativeName("Test");

        User user = new User();
        user.setEmail(USER_EMAIL);
        Principal principal = () -> USER_EMAIL;
        when(userService.findByEmail(USER_EMAIL)).thenReturn(user);

        ResponseEntity<Object> response = languageResource.create(dto, new MockHttpServletRequest(), principal);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(languageService, never()).save(any(Language.class));
    }

    @Test
    public void create_duplicateEnglishName_returnsConflict() {
        LanguageDTO dto = new LanguageDTO();
        dto.setEnglishName("english");
        dto.setNativeName("English");

        User user = new User();
        user.setEmail(USER_EMAIL);
        Principal principal = () -> USER_EMAIL;
        when(userService.findByEmail(USER_EMAIL)).thenReturn(user);
        Language existing = new Language();
        existing.setUuid("existing-uuid");
        existing.setEnglishName("English");
        existing.setNativeName("English");
        when(languageService.findActiveByEnglishName("english")).thenReturn(existing);
        LanguageDTO existingDto = new LanguageDTO();
        existingDto.setUuid("existing-uuid");
        existingDto.setEnglishName("English");
        existingDto.setNativeName("English");
        when(languageAssembler.createDto(existing)).thenReturn(existingDto);

        ResponseEntity<Object> response = languageResource.create(dto, new MockHttpServletRequest(), principal);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(existingDto, response.getBody());
        verify(languageService, never()).save(any(Language.class));
    }

    @Test
    public void create_uniqueEnglishName_setsCreatedByAndSaves() {
        LanguageDTO dto = new LanguageDTO();
        dto.setEnglishName("Klingon");
        dto.setNativeName("tlhIngan Hol");

        Language model = new Language();
        model.setEnglishName("Klingon");
        model.setNativeName("tlhIngan Hol");
        when(languageAssembler.createModel(dto)).thenReturn(model);
        when(languageService.save(any(Language.class))).thenAnswer(invocation -> invocation.getArgument(0));
        LanguageDTO savedDto = new LanguageDTO();
        savedDto.setUuid("new-uuid");
        savedDto.setEnglishName("Klingon");
        savedDto.setNativeName("tlhIngan Hol");
        when(languageAssembler.createDto(any(Language.class))).thenReturn(savedDto);

        User user = new User();
        user.setEmail(USER_EMAIL);
        Principal principal = () -> USER_EMAIL;
        when(userService.findByEmail(USER_EMAIL)).thenReturn(user);

        ResponseEntity<Object> response = languageResource.create(dto, new MockHttpServletRequest(), principal);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(savedDto, response.getBody());
        ArgumentCaptor<Language> captor = ArgumentCaptor.forClass(Language.class);
        verify(languageService).save(captor.capture());
        Language saved = captor.getValue();
        assertEquals(user, saved.getCreatedBy());
        assertNotNull(saved.getSongs());
        assertEquals(Collections.emptyList(), saved.getSongs());
    }
}
