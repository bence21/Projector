package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SongLyricCharCounterTest {

    @Test
    public void count_sumsVerseTexts_excludesTitle() {
        Song song = songWithVerses("Title not counted", "abc", "de");
        Assert.assertEquals(5, SongLyricCharCounter.count(song));
        Assert.assertTrue(SongLyricCharCounter.isShortLyrics(song, SongLyricCharCounter.SHORT_LYRIC_MAX_CHARS));
    }

    @Test
    public void count_nullSongOrVerses_isZero() {
        Assert.assertEquals(0, SongLyricCharCounter.count(null));
        Song song = new Song();
        song.setTitle("x");
        Assert.assertEquals(0, SongLyricCharCounter.count(song));
    }

    @Test
    public void isShortLyrics_falseWhenOverMax() {
        Song song = songWithVerses("t", "a".repeat(301));
        Assert.assertFalse(SongLyricCharCounter.isShortLyrics(song, 300));
        Assert.assertTrue(SongLyricCharCounter.isShortLyrics(song, 301));
    }

    private static Song songWithVerses(String title, String... verseTexts) {
        Song song = new Song();
        song.setTitle(title);
        List<SongVerse> verses = new ArrayList<>();
        for (String text : verseTexts) {
            SongVerse verse = new SongVerse();
            verse.setText(text);
            verses.add(verse);
        }
        song.setVerses(verses);
        return song;
    }
}
