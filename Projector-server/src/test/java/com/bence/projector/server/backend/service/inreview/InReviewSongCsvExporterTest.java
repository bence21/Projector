package com.bence.projector.server.backend.service.inreview;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.service.SongWordValidationService;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InReviewSongCsvExporterTest {

    @Test
    public void escape_quotesCommaAndQuotes() {
        Assert.assertEquals("", InReviewSongCsvExporter.escape(null));
        Assert.assertEquals("plain", InReviewSongCsvExporter.escape("plain"));
        Assert.assertEquals("\"a,b\"", InReviewSongCsvExporter.escape("a,b"));
        Assert.assertEquals("\"a\"\"b\"", InReviewSongCsvExporter.escape("a\"b"));
    }

    @Test
    public void export_includesHeaderAndTitle() {
        Song song = new Song();
        song.setUuid("id-1");
        song.setTitle("Holy, Holy");
        song.setVerses(new ArrayList<>());
        SongWordValidationService validationService = mock(SongWordValidationService.class);
        when(validationService.hasBannedOrRejectedWords(any(Song.class))).thenReturn(false);
        String csv = InReviewSongCsvExporter.export(List.of(song), validationService);
        Assert.assertTrue(csv.startsWith("id,title,lyricCharCount,hasBannedOrRejected,modifiedDate\n"));
        Assert.assertTrue(csv.contains("\"Holy, Holy\""));
        Assert.assertTrue(csv.contains("id-1"));
        Assert.assertTrue(csv.contains(",false,"));
    }
}
