package com.bence.projector.server.utils;

import org.junit.Test;

import static com.bence.projector.server.utils.StringUtils.fixQuotationMarks;
import static com.bence.projector.server.utils.StringUtils.format;
import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

    @Test
    public void test_format() {
        String s = format("4. Megostorozva állott, Nézd: \"Íme az Ember. \" -");
        assertEquals("4. Megostorozva állott, Nézd: \"Íme az Ember.\" -", s);
    }

    @Test
    public void test_format2() {
        String s = format("„Jövel - így szól a Lélek,-");
        assertEquals("„Jövel – így szól a Lélek, –", s);
    }

    @Test
    public void test_format3() {
        String s = format("hogy élsz bennem. :|");
        assertEquals("hogy élsz bennem. :|", s);
    }

    @Test
    public void test_format4() {
        String s = format("Óh nagy Isten oh mily dicső! /: Mit bölcsességgel alkotál. :\\\\ 3x a föld teljes, teljes");
        assertEquals("Óh nagy Isten oh mily dicső! /: Mit bölcsességgel alkotál. :/ 3x a föld teljes, teljes", s);
    }

    @Test
    public void test_format5() {
        String s = format("Ezt mondom hogy: „látok,” bárha");
        assertEquals("Ezt mondom hogy: „látok,” bárha", s);
    }

    @Test
    public void test_format6() {
        String s = format("Ezt mondom hogy: „látok, ” bárha");
        assertEquals("Ezt mondom hogy: „látok,” bárha", s);
    }

    @Test
    public void test_format7() {
        String s = format("Enyém vagy, mert megváltottalak. ”");
        assertEquals("Enyém vagy, mert megváltottalak.”", s);
    }

    @Test
    public void test_format8() {
        String s = format("”Később érted meg ezt!\"");
        assertEquals("“Később érted meg ezt!”", s);
    }

    @Test
    public void test_format9() {
        String s = format("Ezt mondom hogy: „látok, ” bárha");
        assertEquals("Ezt mondom hogy: „látok,” bárha", s);
    }

    @Test
    public void test_format10() {
        String s = format("„Hol vagy!?\" - kiált az Úr neked,");
        assertEquals("„Hol vagy!?” – kiált az Úr neked,", s);
    }

    @Test
    public void test_format11() {
        String s = format("Kik zengik „  Dicsőség Istennek\"");
        assertEquals("Kik zengik „Dicsőség Istennek”", s);
    }

    @Test
    public void test_format12() {
        String s = format("„Hol vagy!? \"- kiált az Úr neked,");
        assertEquals("„Hol vagy!?” – kiált az Úr neked,", s);
    }

    @Test
    public void test_format13() {
        String s = format("“én”-t");
        assertEquals("“én”-t", s);
    }

    @Test
    public void test_format14() {
        String s = format("asdf\n  - „Aki akar, jöjjön ma!”");
        assertEquals("asdf\n– „Aki akar, jöjjön ma!”", s);
    }

    @Test
    public void test_format15() {
        String s = format("-mert úgy szeretett-,");
        assertEquals("– mert úgy szeretett –,", s);
    }

    @Test
    public void test_format16() {
        String s = format("Igen, Uram! - ezt felelem-");
        assertEquals("Igen, Uram! – ezt felelem –", s);
    }

    @Test
    public void test_format17() {
        String s = format("\" Hozzám kiálts!\" - Így szól az Úr tenéked, -");
        assertEquals("\"Hozzám kiálts!\" – Így szól az Úr tenéked, –", s);
    }

    @Test
    public void test_format18() {
        String s = format("„Jövel- így szól a Lélek,-");
        assertEquals("„Jövel – így szól a Lélek, –", s);
    }

    @Test
    public void test_format19() {
        String s = format("Nem vonzódol-e felé?");
        assertEquals("Nem vonzódol-e felé?", s);
    }

    @Test
    public void test_format20() {
        String s = format("halld! „Add ide");
        assertEquals("halld! „Add ide", s);
    }

    @Test
    public void test_format21() {
        String s = format("Hisz neked győznöd kell!“ :/");
        assertEquals("Hisz neked győznöd kell! “ :/", s);
    }

    @Test
    public void test_format22() {
        String s = format("Hisz neked győznöd kell! ” :/");
        assertEquals("Hisz neked győznöd kell!” :/", s);
    }

    @Test
    public void test_format23() {
        String s = format("Én veletek vagyok! \"  ");
        assertEquals("Én veletek vagyok!\"", s);
    }

    @Test
    public void test_format24() {
        String s = format("""
                „Názáreti Jézus már elment!
                ”Késő!„a föld e jajtól reng –
                ”Názáreti Jézus már elment?”""");
        assertEquals("""
                „Názáreti Jézus már elment!
                ”Késő! „a föld e jajtól reng –
                ”Názáreti Jézus már elment?”""", s);
    }

    @Test
    public void test_format25() {
        String s = format("""
                2. Hogyha nékem is el kell majd mennem
                Mória hegyére fel, Ne hagyd,
                Uram, akkor ezt felednem:
                „Az Úr majd gondot visel!”
                Kegyelméből "kirendelte" Isten
                A szent Bárányt, hogy rajtunk segítsen.
                Feláldozta őt a fán,
                Kínnal teljes Golgotán.""");
        assertEquals("""
                2. Hogyha nékem is el kell majd mennem
                Mória hegyére fel, Ne hagyd,
                Uram, akkor ezt felednem:
                „Az Úr majd gondot visel!”
                Kegyelméből „kirendelte” Isten
                A szent Bárányt, hogy rajtunk segítsen.
                Feláldozta őt a fán,
                Kínnal teljes Golgotán.""", s);
    }

    @Test
    public void test_format26() {
        String s = format("Mein ganzes Leben lang habe ich gesucht\r aber es ist schwer, den Weg zu finden\r vorbei am Ziel vor mir\nwährend das Wichtige einfach wegrutscht\r \nEs kommt nicht wieder, aber ich werde suchen\r alles in meinem Leben");
        assertEquals("Mein ganzes Leben lang habe ich gesucht\naber es ist schwer, den Weg zu finden\nvorbei am Ziel vor mir\nwährend das Wichtige einfach wegrutscht\nEs kommt nicht wieder, aber ich werde suchen\nalles in meinem Leben", s);
    }

    @Test
    public void test_format27() {
        String s = format("Set 'em up");
        assertEquals("Set 'em up", s);
    }

    @Test
    public void test_fixQuotationMarks() {
        String s = fixQuotationMarks("This is a string with „quotes”. Another ”example”.");
        assertEquals("This is a string with „quotes”. Another „example”.", s);
    }

    @Test
    public void test_fixQuotationMarks2() {
        String s = fixQuotationMarks("This is a string with ”quotes”. Another „example”.");
        assertEquals("This is a string with „quotes”. Another „example”.", s);
    }

    @Test
    public void test_fixQuotationMarks3() {
        String s = fixQuotationMarks("This ”is a string with „quotes”. Another” ”example”.");
        assertEquals("This „is a string with ”quotes„. Another” „example”.", s);
    }
}