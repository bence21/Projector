package projector.controller.biblesearch;

import org.junit.Assert;
import org.junit.Test;
import projector.model.BibleVerse;

public class BibleSearchMatcherTest {

    @Test
    public void matchesSubstringByDefault() {
        Assert.assertTrue(BibleSearchMatcher.matches("hello world", "world", false));
        Assert.assertFalse(BibleSearchMatcher.matches("hello world", "wor", true));
    }

    @Test
    public void matchesWholeWordAtBoundaries() {
        Assert.assertTrue(BibleSearchMatcher.matches("seek and find", "and", true));
        Assert.assertFalse(BibleSearchMatcher.matches("land", "and", true));
    }

    @Test
    public void normalizesWithoutAccents() {
        String query = BibleSearchMatcher.normalizeQuery("Ádám", false, false);
        Assert.assertEquals("adam", query);
    }

    @Test
    public void verseTextUsesStrippedTextWhenAvailable() {
        BibleVerse verse = new BibleVerse();
        verse.setText("Ádám");
        verse.setStrippedText("adam");
        Assert.assertEquals("adam", BibleSearchMatcher.verseTextForSearch(verse, false, false));
    }
}
