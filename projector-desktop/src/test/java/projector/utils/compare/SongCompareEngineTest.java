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
    public void findBestMatchingVerseIndex_matchesAcrossVerseSplitBoundaries() {
        CompareSongsSettings settings = new CompareSongsSettings();
        // Same words as a single verse, but the counterpart stores them as two verses.
        String focus = "Erőt adsz minden helyzetben\nTe vagy az Úr";
        List<SongVerse> splitVerses = verses(
                "Erőt adsz minden helyzetben",
                "Te vagy az Úr",
                "Más versszak");

        Assert.assertEquals(-1, SongCompareEngine.findMatchingVerseIndex(focus, splitVerses, -1, settings));
        Assert.assertEquals(0, SongCompareEngine.findBestMatchingVerseIndex(focus, splitVerses, -1, settings));
    }

    @Test
    public void findBestMatchingVerseIndex_matchesWhenFocusIsSubsetAfterResplit() {
        CompareSongsSettings settings = new CompareSongsSettings();
        String focus = "Erőt adsz minden helyzetben";
        List<SongVerse> mergedVerses = verses(
                "Erőt adsz minden helyzetben\nTe vagy az Úr",
                "Más versszak");

        Assert.assertEquals(0, SongCompareEngine.findBestMatchingVerseIndex(focus, mergedVerses, -1, settings));
    }

    @Test
    public void versionsLookVeryDifferent_ignoresSplitOnlyDifferences() {
        CompareSongsSettings settings = new CompareSongsSettings();
        Song left = songWithVerses(
                "Erőt adsz minden helyzetben\nTe vagy az Úr",
                "Második versszak szövege itt");
        Song right = songWithVerses(
                "Erőt adsz minden helyzetben",
                "Te vagy az Úr",
                "Második versszak szövege itt");

        Assert.assertFalse(SongCompareEngine.versionsLookVeryDifferent(left, right, settings));
    }

    @Test
    public void versionsLookVeryDifferent_whenWordingTrulyDiffers() {
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
