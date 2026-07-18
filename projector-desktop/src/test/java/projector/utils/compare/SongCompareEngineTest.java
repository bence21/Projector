package projector.utils.compare;

import org.junit.Assert;
import org.junit.Test;
import projector.model.Song;
import projector.model.SongVerse;

import java.util.ArrayList;
import java.util.List;

public class SongCompareEngineTest {

    @Test
    public void findMatchingVerseIndex_matchesReorderedVerseByText() {
        CompareSongsSettings settings = new CompareSongsSettings();
        List<SongVerse> verses = verses("Alpha", "Bravo", "Charlie");

        Assert.assertEquals(2, SongCompareEngine.findMatchingVerseIndex("Charlie", verses, -1, settings));
        Assert.assertEquals(0, SongCompareEngine.findMatchingVerseIndex("Alpha", verses, -1, settings));
        Assert.assertEquals(-1, SongCompareEngine.findMatchingVerseIndex("Missing", verses, -1, settings));
    }

    @Test
    public void versionsLookVeryDifferent_whenMostVersesDiffer() {
        CompareSongsSettings settings = new CompareSongsSettings();
        Song left = songWithVerses("One", "Two", "Three", "Four");
        Song similar = songWithVerses("One", "Two", "Three", "Four");
        Song different = songWithVerses("A", "B", "C", "D");

        Assert.assertFalse(SongCompareEngine.versionsLookVeryDifferent(left, similar, settings));
        Assert.assertTrue(SongCompareEngine.versionsLookVeryDifferent(left, different, settings));
    }

    private static Song songWithVerses(String... texts) {
        Song song = new Song();
        song.setVerses(verses(texts));
        return song;
    }

    private static List<SongVerse> verses(String... texts) {
        List<SongVerse> verses = new ArrayList<>();
        for (String text : texts) {
            SongVerse verse = new SongVerse();
            verse.setText(text);
            verses.add(verse);
        }
        return verses;
    }
}
