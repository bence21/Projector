package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.utils.models.SongWord;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static com.bence.projector.server.utils.SetLanguages.addWordsInCollection;

/**
 * Regression tests for {@link SetLanguages#addWordsInCollection} null-safety.
 */
public class SetLanguagesAddWordsCollectionTest {

    @Test
    public void addWordsInCollection_whenVersesNull_returnsWithoutException() {
        Song song = new Song();
        ArrayList<SongWord> words = new ArrayList<>();
        addWordsInCollection(song, words);
        Assert.assertTrue(words.isEmpty());
    }

    @Test
    public void addWordsInCollection_whenVerseEntryNull_skipsNull() throws Exception {
        Song song = new Song();
        SongVerse v = new SongVerse();
        v.setText("hello world");
        ArrayList<SongVerse> verses = new ArrayList<>();
        verses.add(v);
        verses.add(null);
        Field f = Song.class.getDeclaredField("verses");
        f.setAccessible(true);
        f.set(song, verses);
        ArrayList<SongWord> words = new ArrayList<>();
        addWordsInCollection(song, words);
        Assert.assertFalse(words.isEmpty());
    }
}
