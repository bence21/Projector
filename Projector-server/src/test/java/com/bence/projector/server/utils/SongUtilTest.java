package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.model.SongCollectionElement;
import com.bence.projector.server.backend.model.SongLink;
import com.bence.projector.server.backend.repository.SongLinkRepository;
import com.bence.projector.server.backend.service.SongCollectionElementService;
import com.bence.projector.server.backend.service.SongCollectionService;
import com.bence.projector.server.backend.service.SongLinkService;
import com.bence.projector.server.backend.service.SongService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SongUtilTest {

    @Mock
    private SongService songService;
    @Mock
    private SongLinkService songLinkService;
    @Mock
    private SongLinkRepository songLinkRepository;
    @Mock
    private SongCollectionService songCollectionService;
    @Mock
    private SongCollectionElementService songCollectionElementService;

    @Test
    public void transferSongCollectionElements_repointsMembershipWhenSurvivorNotInCollection() {
        Song loser = publicSong(1L, "loser-uuid");
        Song survivor = publicSong(2L, "survivor-uuid");

        SongCollection collection = new SongCollection();
        List<SongCollectionElement> elements = new ArrayList<>();
        SongCollectionElement el = element(10L, "42", loser, collection);
        elements.add(el);
        collection.setSongCollectionElements(elements);

        when(songCollectionService.findAllBySong(loser)).thenReturn(Collections.singletonList(collection));

        SongUtil.transferSongCollectionElements(loser, survivor, songCollectionService, songCollectionElementService);

        Assert.assertEquals(survivor.getUuid(), el.getSongUuid());
        verify(songCollectionElementService).save(el);
        verify(songCollectionElementService, never()).delete(anyLong());
        verify(songCollectionService).saveWithoutForeign(collection);
    }

    @Test
    public void transferSongCollectionElements_repointsLoserRowEvenWhenSurvivorAlreadyInSameCollection() {
        Song loser = publicSong(1L, "loser-uuid");
        Song survivor = publicSong(2L, "survivor-uuid");

        SongCollection collection = new SongCollection();
        List<SongCollectionElement> elements = new ArrayList<>();
        SongCollectionElement survivorEl = element(20L, "1", survivor, collection);
        SongCollectionElement loserEl = element(21L, "99", loser, collection);
        elements.add(survivorEl);
        elements.add(loserEl);
        collection.setSongCollectionElements(elements);

        when(songCollectionService.findAllBySong(loser)).thenReturn(Collections.singletonList(collection));

        SongUtil.transferSongCollectionElements(loser, survivor, songCollectionService, songCollectionElementService);

        Assert.assertEquals("1", survivorEl.getOrdinalNumber());
        Assert.assertEquals(survivor.getUuid(), loserEl.getSongUuid());
        Assert.assertEquals("99", loserEl.getOrdinalNumber());
        verify(songCollectionElementService).save(loserEl);
        verify(songCollectionElementService, never()).save(survivorEl);
        verify(songCollectionElementService, never()).delete(anyLong());
        verify(songCollectionService).saveWithoutForeign(collection);
    }

    @Test
    public void handleSimilarLoadedSong_nearDuplicateOneNonPublic_transfersAndDeletesLoser() {
        Song anchor = publicSong(1L, "anchor");
        Song similar = nonPublicSong(2L, "similar");

        when(songCollectionService.findAllBySong(similar)).thenReturn(Collections.emptyList());

        SongUtil.handleSimilarLoadedSong(
                anchor, similar, null, songService, songLinkService, 0.995, songLinkRepository,
                songCollectionService, songCollectionElementService, null);

        verify(songCollectionService).findAllBySong(similar);
        verify(songService).deleteByUuid("similar");
        verify(songLinkService, never()).save(any(SongLink.class));
    }

    @Test
    public void handleSimilarLoadedSong_nearDuplicate_skipsTransferAndDeleteWhenLoserOlderThanOneDay() {
        Song anchor = publicSong(1L, "anchor");
        Song similar = nonPublicSong(2L, "similar");
        similar.setCreatedDate(new Date(System.currentTimeMillis() - 3L * 24 * 60 * 60 * 1000));

        SongUtil.handleSimilarLoadedSong(
                anchor, similar, null, songService, songLinkService, 0.995, songLinkRepository,
                songCollectionService, songCollectionElementService, null);

        verify(songCollectionService, never()).findAllBySong(any());
        verify(songService, never()).deleteByUuid(any());
        verify(songLinkService, never()).save(any(SongLink.class));
    }

    @Test
    public void isLoserRecentEnoughForCollectionTransfer_trueWithin24h() {
        Song s = publicSong(1L, "x");
        Assert.assertTrue(SongUtil.isLoserRecentEnoughForCollectionTransfer(s));
    }

    @Test
    public void isLoserRecentEnoughForCollectionTransfer_falseWhenCreatedNull() {
        Song s = publicSong(1L, "x");
        s.setCreatedDate(null);
        Assert.assertFalse(SongUtil.isLoserRecentEnoughForCollectionTransfer(s));
    }

    @Test
    public void isLoserEligibleForNearDuplicate_falseWhenCreatedBeforeCutoff() {
        Song s = publicSong(1L, "x");
        s.setCreatedDate(Date.from(LocalDate.of(2025, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        Date cutoff = Date.from(LocalDate.of(2026, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Assert.assertFalse(SongUtil.isLoserEligibleForNearDuplicateCollectionTransfer(s, cutoff));
    }

    @Test
    public void isLoserEligibleForNearDuplicate_trueWhenCreatedOnCutoffDay() {
        Song s = publicSong(1L, "x");
        Date cutoff = Date.from(LocalDate.of(2026, 1, 15).atStartOfDay(ZoneId.systemDefault()).toInstant());
        s.setCreatedDate(Date.from(LocalDate.of(2026, 1, 15).atTime(14, 30).atZone(ZoneId.systemDefault()).toInstant()));
        Assert.assertTrue(SongUtil.isLoserEligibleForNearDuplicateCollectionTransfer(s, cutoff));
    }

    @Test
    public void handleSimilarLoadedSong_nearDuplicate_transfersWhenLoserOlderThan24hButAfterAbsoluteCutoff() {
        Song anchor = publicSong(1L, "anchor");
        Song similar = nonPublicSong(2L, "similar");
        similar.setCreatedDate(Date.from(LocalDate.of(2026, 2, 15).atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant()));

        Date cutoff = Date.from(LocalDate.of(2026, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        when(songCollectionService.findAllBySong(similar)).thenReturn(Collections.emptyList());

        SongUtil.handleSimilarLoadedSong(
                anchor, similar, null, songService, songLinkService, 0.995, songLinkRepository,
                songCollectionService, songCollectionElementService, cutoff);

        verify(songCollectionService).findAllBySong(similar);
        verify(songService).deleteByUuid("similar");
    }

    @Test
    public void handleSimilarLoadedSong_nearDuplicate_skipsWhenBeforeAbsoluteCutoffEvenIfWithin24h() {
        Song anchor = publicSong(1L, "anchor");
        Song similar = nonPublicSong(2L, "similar");
        similar.setCreatedDate(new Date(System.currentTimeMillis() - 2L * 60 * 60 * 1000));

        Date cutoff = new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000);

        SongUtil.handleSimilarLoadedSong(
                anchor, similar, null, songService, songLinkService, 0.995, songLinkRepository,
                songCollectionService, songCollectionElementService, cutoff);

        verify(songCollectionService, never()).findAllBySong(any());
        verify(songService, never()).deleteByUuid(any());
    }

    @Test
    public void handleSimilarLoadedSong_nearDuplicateBothPublic_attemptsVersionGroupMerge() {
        Song a = publicSong(1L, "a");
        Song b = publicSong(2L, "b");
        a.setModifiedDate(new Date(1));
        b.setModifiedDate(new Date(2));

        when(songService.findAllByVersionGroup("a")).thenReturn(Collections.singletonList(a));
        when(songService.findAllByVersionGroup("b")).thenReturn(Collections.singletonList(b));

        SongUtil.handleSimilarLoadedSong(
                a, b, null, songService, songLinkService, 0.991, songLinkRepository,
                songCollectionService, songCollectionElementService, null);

        verify(songService, times(2)).findAllByVersionGroup(any());
        verify(songCollectionService, never()).findAllBySong(any());
        verify(songLinkService, never()).save(any(SongLink.class));
    }

    @Test
    public void handleSimilarLoadedSong_nearDuplicateBothPublic_fallbackToSongLinkWhenMergeSkippedForSize() {
        Song a = publicSong(1L, "a");
        Song b = publicSong(2L, "b");

        List<Song> largeGroup = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Song s = publicSong(100L + i, "m-" + i);
            largeGroup.add(s);
        }
        List<Song> largeGroup2 = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Song s = publicSong(200L + i, "n-" + i);
            largeGroup2.add(s);
        }

        when(songService.findAllByVersionGroup("a")).thenReturn(largeGroup);
        when(songService.findAllByVersionGroup("b")).thenReturn(largeGroup2);
        when(songLinkRepository.findAllBySong1OrSong2(eq(a), eq(a))).thenReturn(Collections.emptyList());

        SongUtil.handleSimilarLoadedSong(
                a, b, null, songService, songLinkService, 0.991, songLinkRepository,
                songCollectionService, songCollectionElementService, null);

        ArgumentCaptor<SongLink> linkCaptor = ArgumentCaptor.forClass(SongLink.class);
        verify(songLinkService).save(linkCaptor.capture());
        SongLink saved = linkCaptor.getValue();
        Assert.assertEquals("a", saved.getSong1Uuid());
        Assert.assertEquals("b", saved.getSong2Uuid());
    }

    @Test
    public void handleSimilarLoadedSong_nearDuplicateBothNonPublic_createsSongLink() {
        Song a = nonPublicSong(1L, "a");
        Song b = nonPublicSong(2L, "b");

        when(songLinkRepository.findAllBySong1OrSong2(eq(a), eq(a))).thenReturn(Collections.emptyList());

        SongUtil.handleSimilarLoadedSong(
                a, b, null, songService, songLinkService, 0.995, songLinkRepository,
                songCollectionService, songCollectionElementService, null);

        verify(songLinkService).save(any(SongLink.class));
        verify(songService, never()).deleteByUuid(any());
        verify(songCollectionService, never()).findAllBySong(any());
    }

    @Test
    public void songCollection_getElementsForSongUuid_returnsOnlyMatchingRows() {
        SongCollection collection = new SongCollection();
        Song a = publicSong(1L, "song-a");
        Song b = publicSong(2L, "song-b");
        collection.getSongCollectionElements().add(element(10L, "1", a, collection));
        collection.getSongCollectionElements().add(element(11L, "2", b, collection));

        List<SongCollectionElement> forA = collection.getElementsForSongUuid("song-a");
        Assert.assertEquals(1, forA.size());
        Assert.assertEquals("1", forA.get(0).getOrdinalNumber());
        Assert.assertTrue(collection.getElementsForSongUuid(null).isEmpty());
    }

    private static Song publicSong(Long id, String uuid) {
        Song song = new Song();
        song.setId(id);
        song.setUuid(uuid);
        song.setDeleted(false);
        song.setReviewerErased(null);
        song.setIsBackUp(null);
        song.setHasUnsolvedWords(null);
        song.setModifiedDate(new Date());
        song.setCreatedDate(new Date());
        song.setVerses(new ArrayList<>());
        return song;
    }

    private static Song nonPublicSong(Long id, String uuid) {
        Song song = publicSong(id, uuid);
        song.setHasUnsolvedWords(true);
        return song;
    }

    private static SongCollectionElement element(Long id, String ordinal, Song song, SongCollection collection) {
        SongCollectionElement el = new SongCollectionElement();
        el.setId(id);
        el.setOrdinalNumber(ordinal);
        el.setSong(song);
        el.setSongCollection(collection);
        return el;
    }
}
