package projector.controller.song.util;

import org.junit.Assert;
import org.junit.Test;
import projector.model.Language;
import projector.model.Song;

import java.util.ArrayList;
import java.util.List;

public class ScheduleSongMatcherTest {

    @Test
    public void exactSingle_match() {
        Song s = new Song();
        s.setTitle("Amazing Grace");
        List<Song> list = List.of(s);
        ScheduleSongMatcher.Result r = ScheduleSongMatcher.match("Amazing Grace", list);
        Assert.assertEquals(ScheduleSongMatcher.MatchStatus.MATCHED, r.status());
        Assert.assertSame(s, r.song());
    }

    @Test
    public void multipleExact_prefersFavourite() {
        Song a = new Song();
        a.setTitle("Holy");
        Song b = new Song();
        b.setTitle("Holy");
        b.setFavourite(true);
        List<Song> list = new ArrayList<>();
        list.add(a);
        list.add(b);
        ScheduleSongMatcher.Result r = ScheduleSongMatcher.match("Holy", list);
        Assert.assertEquals(ScheduleSongMatcher.MatchStatus.MATCHED, r.status());
        Assert.assertSame(b, r.song());
    }

    @Test
    public void multipleSubstring_ambiguous_picksBestRankedMatch() {
        Song a = new Song();
        a.setTitle("Amazing Grace");
        Song b = new Song();
        b.setTitle("Amazing Grain");
        List<Song> list = List.of(a, b);
        ScheduleSongMatcher.Result r = ScheduleSongMatcher.match("Amazing Gra", list);
        Assert.assertEquals(ScheduleSongMatcher.MatchStatus.AMBIGUOUS, r.status());
        Assert.assertEquals(2, r.alternatives().size());
        Assert.assertSame(a, r.song());
        Assert.assertSame(a, r.alternatives().get(0));
    }

    @Test
    public void nothingFound() {
        Song s = new Song();
        s.setTitle("Other");
        ScheduleSongMatcher.Result r = ScheduleSongMatcher.match("Missing", List.of(s));
        Assert.assertEquals(ScheduleSongMatcher.MatchStatus.NOT_FOUND, r.status());
        Assert.assertNull(r.song());
    }

    @Test
    public void exactMatch_handlesExtraWhitespaceInTitle() {
        Song s = new Song();
        s.setTitle("  Amazing    Grace  ");
        ScheduleSongMatcher.Result r = ScheduleSongMatcher.match("Amazing Grace", List.of(s));
        Assert.assertEquals(ScheduleSongMatcher.MatchStatus.MATCHED, r.status());
        Assert.assertSame(s, r.song());
    }

    @Test
    public void selectedSong_withVersionGroup_overridesToFavouriteInSameLanguage() {
        Language english = language(1L, "lang-en");
        Song selected = song("Only Hope", "vg-hope", english, false);
        Song favourite = song("Only Hope (Fav)", "vg-hope", english, true);

        ScheduleSongMatcher.Result r = ScheduleSongMatcher.match("Only Hope", List.of(selected, favourite));

        Assert.assertEquals(ScheduleSongMatcher.MatchStatus.MATCHED, r.status());
        Assert.assertSame(favourite, r.song());
        Assert.assertTrue(r.selectedFavourite());
        Assert.assertTrue(r.overriddenToFavourite());
    }

    @Test
    public void selectedSong_withVersionGroup_doesNotOverrideWhenLanguageDiffers() {
        Language english = language(1L, "lang-en");
        Language hungarian = language(2L, "lang-hu");
        Song selected = song("Way Maker", "vg-way-maker", english, false);
        Song favouriteOtherLanguage = song("Way Maker HU", "vg-way-maker", hungarian, true);

        ScheduleSongMatcher.Result r = ScheduleSongMatcher.match("Way Maker", List.of(selected, favouriteOtherLanguage));

        Assert.assertEquals(ScheduleSongMatcher.MatchStatus.MATCHED, r.status());
        Assert.assertSame(selected, r.song());
        Assert.assertFalse(r.selectedFavourite());
        Assert.assertFalse(r.overriddenToFavourite());
    }

    @Test
    public void selectedSong_withoutVersionGroup_doesNotOverride() {
        Language english = language(1L, "lang-en");
        Song selected = song("Cornerstone", null, english, false);
        Song favouriteInDifferentGroup = song("Cornerstone Fav", "vg-cornerstone", english, true);

        ScheduleSongMatcher.Result r = ScheduleSongMatcher.match("Cornerstone", List.of(selected, favouriteInDifferentGroup));

        Assert.assertEquals(ScheduleSongMatcher.MatchStatus.MATCHED, r.status());
        Assert.assertSame(selected, r.song());
        Assert.assertFalse(r.selectedFavourite());
        Assert.assertFalse(r.overriddenToFavourite());
    }

    @Test
    public void selectedSong_withoutVersionGroup_usesUuidAsFallbackGroupForOverride() {
        Language english = language(1L, "lang-en");
        Song selected = song("Chain Breaker", null, english, false);
        selected.setUuid("song-uuid-1");
        Song favouriteByUuidGroup = song("Chain Breaker (Fav)", "song-uuid-1", english, true);

        ScheduleSongMatcher.Result r = ScheduleSongMatcher.match("Chain Breaker", List.of(selected, favouriteByUuidGroup));

        Assert.assertEquals(ScheduleSongMatcher.MatchStatus.MATCHED, r.status());
        Assert.assertSame(favouriteByUuidGroup, r.song());
        Assert.assertTrue(r.selectedFavourite());
        Assert.assertTrue(r.overriddenToFavourite());
    }

    @Test
    public void selectedSong_withVersionGroup_doesNotOverrideWhenNoFavouriteInGroup() {
        Language english = language(1L, "lang-en");
        Song selected = song("Build My Life", "vg-build", english, false);
        Song sameGroupNonFavourite = song("Build My Life Live", "vg-build", english, false);

        ScheduleSongMatcher.Result r = ScheduleSongMatcher.match("Build My Life", List.of(selected, sameGroupNonFavourite));

        Assert.assertEquals(ScheduleSongMatcher.MatchStatus.MATCHED, r.status());
        Assert.assertSame(selected, r.song());
        Assert.assertFalse(r.selectedFavourite());
        Assert.assertFalse(r.overriddenToFavourite());
    }

    private static Song song(String title, String versionGroup, Language language, boolean favourite) {
        Song song = new Song();
        song.setTitle(title);
        song.setVersionGroup(versionGroup);
        song.setLanguage(language);
        song.setFavourite(favourite);
        return song;
    }

    private static Language language(Long id, String uuid) {
        Language language = new Language();
        language.setId(id);
        language.setUuid(uuid);
        return language;
    }
}
