package com.bence.projector.server.backend.service.inreview;

import com.bence.projector.common.dto.InReviewSongFilterCountsDTO;
import com.bence.projector.common.dto.PageResponse;
import com.bence.projector.common.dto.SongTitleDTO;
import com.bence.projector.server.api.assembler.SongTitleAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.SongWordValidationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InReviewSongQueryServiceTest {

    @Mock
    private SongService songService;
    @Mock
    private SongWordValidationService songWordValidationService;

    private InReviewSongQueryService queryService;
    private Language language;

    @Before
    public void setUp() {
        queryService = new InReviewSongQueryService(songService, new SongTitleAssembler(), songWordValidationService);
        language = new Language();
        language.setUuid("lang-1");
    }

    @Test
    public void page_withoutFilters_paginatesAndSkipsBannedScan() {
        List<Song> songs = List.of(
                song("a", "short", "Hi"),
                song("b", "medium", "Hello there"),
                song("c", "long", "x".repeat(400))
        );
        when(songService.findAllInReviewByLanguage(language)).thenReturn(songs);

        InReviewSongQuery query = new InReviewSongQuery(language, null, null, false, null, "TITLE", 0, 2);
        PageResponse<SongTitleDTO> page = queryService.page(query);

        Assert.assertEquals(3, page.getTotalElements());
        Assert.assertEquals(2, page.getContent().size());
        Assert.assertEquals("long", page.getContent().get(0).getTitle());
        Assert.assertEquals("medium", page.getContent().get(1).getTitle());
        verify(songWordValidationService, never()).hasBannedOrRejectedWords(any());
        verify(songService, never()).findAllInReviewByLanguageWithVerses(any());
    }

    @Test
    public void page_shortLyricsFilter_keepsOnlyShort() {
        List<Song> songs = List.of(
                song("a", "short", "Hi"),
                song("b", "long", "x".repeat(400))
        );
        when(songService.findAllInReviewByLanguageWithVerses(language)).thenReturn(songs);

        InReviewSongQuery query = new InReviewSongQuery(language, null, 300, false, null, "TITLE", 0, 10);
        PageResponse<SongTitleDTO> page = queryService.page(query);

        Assert.assertEquals(1, page.getTotalElements());
        Assert.assertEquals("short", page.getContent().get(0).getTitle());
        Assert.assertEquals(2, page.getContent().get(0).getLyricCharCount());
    }

    @Test
    public void page_bannedFilter_independentOfLength() {
        Song shortClean = song("a", "short-clean", "Hi");
        Song shortBanned = song("b", "short-banned", "No");
        Song longBanned = song("c", "long-banned", "x".repeat(400));
        when(songService.findAllInReviewByLanguageWithVerses(language))
                .thenReturn(List.of(shortClean, shortBanned, longBanned));
        when(songWordValidationService.hasBannedOrRejectedWords(shortBanned)).thenReturn(true);
        when(songWordValidationService.hasBannedOrRejectedWords(longBanned)).thenReturn(true);
        when(songWordValidationService.hasBannedOrRejectedWords(shortClean)).thenReturn(false);

        InReviewSongQuery query = new InReviewSongQuery(language, null, null, true, null, "TITLE", 0, 10);
        PageResponse<SongTitleDTO> page = queryService.page(query);

        Assert.assertEquals(2, page.getTotalElements());
        Assert.assertEquals("long-banned", page.getContent().get(0).getTitle());
        Assert.assertEquals("short-banned", page.getContent().get(1).getTitle());
    }

    @Test
    public void page_bothFilters_isIntersection() {
        Song shortClean = song("a", "short-clean", "Hi");
        Song shortBanned = song("b", "short-banned", "No");
        Song longBanned = song("c", "long-banned", "x".repeat(400));
        when(songService.findAllInReviewByLanguageWithVerses(language))
                .thenReturn(List.of(shortClean, shortBanned, longBanned));
        when(songWordValidationService.hasBannedOrRejectedWords(shortBanned)).thenReturn(true);
        when(songWordValidationService.hasBannedOrRejectedWords(shortClean)).thenReturn(false);

        InReviewSongQuery query = new InReviewSongQuery(language, null, 300, true, null, "TITLE", 0, 10);
        PageResponse<SongTitleDTO> page = queryService.page(query);

        Assert.assertEquals(1, page.getTotalElements());
        Assert.assertEquals("short-banned", page.getContent().get(0).getTitle());
    }

    @Test
    public void counts_areIndependent() {
        Song shortClean = song("a", "short-clean", "Hi");
        Song shortBanned = song("b", "short-banned", "No");
        Song longBanned = song("c", "long-banned", "x".repeat(400));
        when(songService.findAllInReviewByLanguageWithVerses(language))
                .thenReturn(List.of(shortClean, shortBanned, longBanned));
        when(songWordValidationService.hasBannedOrRejectedWords(shortBanned)).thenReturn(true);
        when(songWordValidationService.hasBannedOrRejectedWords(longBanned)).thenReturn(true);
        when(songWordValidationService.hasBannedOrRejectedWords(shortClean)).thenReturn(false);

        InReviewSongFilterCountsDTO counts = queryService.counts(language, null);
        Assert.assertEquals(3, counts.getTotal());
        Assert.assertEquals(2, counts.getShortLyrics());
        Assert.assertEquals(2, counts.getBannedOrRejected());
    }

    @Test
    public void page_titleSearch_filtersBeforePaging() {
        when(songService.findAllInReviewByLanguage(language)).thenReturn(List.of(
                song("a", "Amazing Grace", "a"),
                song("b", "How Great", "b")
        ));
        InReviewSongQuery query = new InReviewSongQuery(language, null, null, false, "grace", "TITLE", 0, 10);
        PageResponse<SongTitleDTO> page = queryService.page(query);
        Assert.assertEquals(1, page.getTotalElements());
        Assert.assertEquals("Amazing Grace", page.getContent().get(0).getTitle());
    }

    @Test
    public void exportCsv_usesActiveFilters() {
        Song shortBanned = song("b", "short-banned", "No");
        when(songService.findAllInReviewByLanguageWithVerses(language)).thenReturn(List.of(
                song("a", "short-clean", "Hi"),
                shortBanned
        ));
        when(songWordValidationService.hasBannedOrRejectedWords(any(Song.class))).thenAnswer(invocation -> {
            Song song = invocation.getArgument(0);
            return "short-banned".equals(song.getTitle());
        });

        InReviewSongQuery query = new InReviewSongQuery(language, null, 300, true, null, "TITLE", 0, 10);
        String csv = queryService.exportCsv(query);
        Assert.assertTrue(csv.contains("short-banned"));
        Assert.assertFalse(csv.contains("short-clean"));
    }

    private static Song song(String uuid, String title, String verseText) {
        Song song = new Song();
        song.setUuid(uuid);
        song.setTitle(title);
        SongVerse verse = new SongVerse();
        verse.setText(verseText);
        List<SongVerse> verses = new ArrayList<>();
        verses.add(verse);
        song.setVerses(verses);
        return song;
    }
}
