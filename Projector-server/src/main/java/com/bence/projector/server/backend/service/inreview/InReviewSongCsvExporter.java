package com.bence.projector.server.backend.service.inreview;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.service.SongWordValidationService;
import com.bence.projector.server.utils.SongLyricCharCounter;

import java.util.Date;
import java.util.List;

/**
 * CSV export of filtered In-review songs.
 */
public final class InReviewSongCsvExporter {

    private InReviewSongCsvExporter() {
    }

    public static String export(List<Song> songs, SongWordValidationService validationService) {
        StringBuilder csv = new StringBuilder();
        csv.append("id,title,lyricCharCount,hasBannedOrRejected,modifiedDate\n");
        for (Song song : songs) {
            csv.append(escape(song.getUuid())).append(',');
            csv.append(escape(song.getTitle())).append(',');
            csv.append(SongLyricCharCounter.count(song)).append(',');
            csv.append(validationService.hasBannedOrRejectedWords(song)).append(',');
            csv.append(escape(formatDate(song.getModifiedDate()))).append('\n');
        }
        return csv.toString();
    }

    private static String formatDate(Date date) {
        return date == null ? "" : Long.toString(date.getTime());
    }

    static String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuotes = value.indexOf(',') >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;
        String escaped = value.replace("\"", "\"\"");
        if (needsQuotes) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
