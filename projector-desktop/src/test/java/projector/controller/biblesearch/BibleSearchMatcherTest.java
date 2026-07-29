package projector.controller.biblesearch;

import org.junit.jupiter.api.Test;
import projector.model.BibleVerse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BibleSearchMatcherTest {

    @Test
    void matchesCollapsedSubstringWithoutAccents() {
        BibleVerse verse = verse("Mert mondom nektek, mostantól nem láttok engem mindaddig");

        assertTrue(BibleSearchMatcher.matchesVerse(verse, "engemm", false, false, false));
        assertTrue(BibleSearchMatcher.matchesVerse(verse, "engem", false, false, false));
    }

    @Test
    void wholeWordDoesNotMatchAcrossSkippedLetters() {
        BibleVerse verse = verse("láttok engem mindaddig");

        assertFalse(BibleSearchMatcher.matchesVerse(verse, "engemm", false, false, true));
    }

    @Test
    void matchesAcrossWhitespaceInQueryWithoutAccents() {
        BibleVerse verse = verse("Mert mondom nektek, mostantól nem láttok engem mindaddig");

        assertTrue(BibleSearchMatcher.matchesVerse(verse, "engem m", false, false, false));
        assertTrue(BibleSearchMatcher.matchesVerse(verse, "engemm", false, false, false));

        List<BibleSearchMatcher.MatchSpan> spans = BibleSearchMatcher.findMatchSpans(
                verse.getText(), "engemm", false, false, false);
        assertEquals(1, spans.size());
        assertEquals("engem m", verse.getText().substring(spans.get(0).start(), spans.get(0).endExclusive()));
    }

    @Test
    void wholeWordRequiresLetterBoundariesWithoutAccents() {
        BibleVerse verse = verse("láttok engem mindaddig");

        assertTrue(BibleSearchMatcher.matchesVerse(verse, "engem", false, false, true));
        assertFalse(BibleSearchMatcher.matchesVerse(verse, "engemm", false, false, true));
        assertFalse(BibleSearchMatcher.matchesVerse(verse, "em", false, false, true));
    }

    @Test
    void wholeWordWorksWithAccents() {
        BibleVerse verse = verse("láttok engem mindaddig");

        assertTrue(BibleSearchMatcher.matchesVerse(verse, "engem", true, false, true));
        assertFalse(BibleSearchMatcher.matchesVerse(verse, "em", true, false, true));
    }

    private static BibleVerse verse(String text) {
        BibleVerse bibleVerse = new BibleVerse();
        bibleVerse.setText(text);
        return bibleVerse;
    }
}
