package projector.controller.song.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SchedulePasteParserTest {

    @Test
    public void normalizeSongCandidate_stripsTrailingChord() {
        Assert.assertEquals("El shaddai", SchedulePasteParser.normalizeSongCandidate("El shaddai. Dm"));
        Assert.assertEquals("Te vagy a kiralyom", SchedulePasteParser.normalizeSongCandidate("Te vagy a kiralyom"));
    }

    @Test
    public void lineMode_emitsSectionLines() {
        String text = "Te vagy a kiralyom\nEl shaddai. Dm\n    Imaora\nEnyem lenne minden fajdalom";
        List<SchedulePasteEntry> entries = SchedulePasteParser.parse(text);
        Assert.assertEquals(SchedulePasteEntry.Kind.SONG, entries.get(0).getKind());
        Assert.assertEquals("Te vagy a kiralyom", entries.get(0).getText());
        Assert.assertEquals(SchedulePasteEntry.Kind.SONG, entries.get(1).getKind());
        Assert.assertEquals("El shaddai", entries.get(1).getText());
        Assert.assertEquals(SchedulePasteEntry.Kind.SECTION, entries.get(2).getKind());
        Assert.assertEquals("Imaora", entries.get(2).getText());
        Assert.assertEquals(SchedulePasteEntry.Kind.SONG, entries.get(3).getKind());
        Assert.assertEquals("Enyem lenne minden fajdalom", entries.get(3).getText());
    }

    @Test
    public void lineMode_emitsEachNonChordLine() {
        String text = "First song\nSzunet\nSecond song";
        List<SchedulePasteEntry> entries = SchedulePasteParser.parse(text);
        Assert.assertEquals(3, entries.size());
        Assert.assertEquals(SchedulePasteEntry.Kind.SONG, entries.get(0).getKind());
        Assert.assertEquals(SchedulePasteEntry.Kind.SECTION, entries.get(1).getKind());
        Assert.assertEquals(SchedulePasteEntry.Kind.SONG, entries.get(2).getKind());
    }

    @Test
    public void chordOnlyLine_skipped() {
        List<SchedulePasteEntry> entries = SchedulePasteParser.parse("Dm");
        Assert.assertTrue(entries.isEmpty());
    }
}
