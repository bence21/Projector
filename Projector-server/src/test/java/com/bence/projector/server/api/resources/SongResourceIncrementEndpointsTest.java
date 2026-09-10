package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.server.api.assembler.SongAssembler;
import com.bence.projector.server.api.assembler.SongTitleAssembler;
import com.bence.projector.server.backend.model.Song;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies incViews / incFavourites delegates to {@link SongService} counter methods (not full save).
 */
public class SongResourceIncrementEndpointsTest {

    private SongService songService;
    private SongAssembler songAssembler;
    private SongResource songResource;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        SongRepository songRepository = mock(SongRepository.class);
        songService = mock(SongService.class);
        songAssembler = mock(SongAssembler.class);
        SongTitleAssembler songTitleAssembler = mock(SongTitleAssembler.class);
        StatisticsService statisticsService = mock(StatisticsService.class);
        UserService userService = mock(UserService.class);
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
        mockMvc = MockMvcBuilders.standaloneSetup(songResource).build();
    }

    @Test
    public void incrementViews_viaMockMvc_callsIncrementViews_andReturnsAccepted() throws Exception {
        Song song = new Song();
        song.setUuid("abc-view");
        song.setViews(3);
        when(songService.incrementViews(eq("abc-view"))).thenReturn(song);

        SongDTO dto = new SongDTO();
        dto.setUuid(song.getUuid());
        dto.setViews(3);
        dto.setFavourites(0L);
        when(songAssembler.createDto(song)).thenReturn(dto);

        mockMvc.perform(put("/api/song/{songId}/incViews", "abc-view").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        verify(songService).incrementViews("abc-view");

        ArgumentCaptor<Song> songCaptor = ArgumentCaptor.forClass(Song.class);
        verify(songAssembler).createDto(songCaptor.capture());
        assertEquals("abc-view", songCaptor.getValue().getUuid());
    }

    @Test
    public void incrementFavourites_viaMockMvc_callsIncrementFavourites() throws Exception {
        Song song = new Song();
        song.setUuid("abc-fav");
        song.setFavourites(7);
        when(songService.incrementFavourites(eq("abc-fav"))).thenReturn(song);

        SongDTO dto = new SongDTO();
        dto.setUuid(song.getUuid());
        dto.setViews(0L);
        dto.setFavourites(7L);
        when(songAssembler.createDto(song)).thenReturn(dto);

        mockMvc.perform(put("/api/song/{songId}/incFavourites", "abc-fav").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());

        verify(songService).incrementFavourites("abc-fav");
    }

    @Test
    public void incrementViews_returnsNoContent_whenSongMissing() {
        when(songService.incrementViews(eq("missing"))).thenReturn(null);

        ResponseEntity<Object> response = songResource.incrementViews("missing", new MockHttpServletRequest());
        assertEquals(204, response.getStatusCodeValue());
    }
}
