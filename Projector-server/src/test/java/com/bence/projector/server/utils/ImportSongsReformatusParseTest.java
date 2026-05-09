package com.bence.projector.server.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link ImportSongs#parseReformatusPlainTextLines(List)} — one empty line = verse break; two = next song.
 */
public class ImportSongsReformatusParseTest {

    @Test
    public void firstSong_splitsVersesBySingleEmptyLine() {
        List<String> lines = new ArrayList<>();
        lines.add("1. Aki nem jár hitlenek tanácsán");
        lines.add("Aki nem jár hitlenek tanácsán,");
        lines.add("És meg nem áll a bűnösök útán,");
        lines.add("A csúfolóknak nem ül ő székében,");
        lines.add("De gyönyörködik az Úr törvényében,");
        lines.add("És arra gondja mind éjjel, nappal:");
        lines.add("Ez ily ember nagy boldog bizonnyal.");
        lines.add("");
        lines.add("Mert ő olyan, mint a jó termőfa,");
        lines.add("Mely a víz mellett vagyon plántálva,");
        lines.add("");
        lines.add("");
        lines.add("2. Miért zúgolódnak a pogányok");
        lines.add("Miért zúgolódnak a pogányok?");

        List<ImportSongs.MRESong> out = ImportSongs.parseReformatusPlainTextLines(lines);
        assertEquals(2, out.size());
        assertEquals("1", out.get(0).number);
        assertEquals("Aki nem jár hitlenek tanácsán", out.get(0).title);
        assertTrue(out.get(0).verses.size() >= 2);
        assertEquals("2", out.get(1).number);
        assertEquals("Miért zúgolódnak a pogányok", out.get(1).title);
        assertEquals(1, out.get(1).verses.size());
    }
}
