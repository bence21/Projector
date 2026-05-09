package projector.controller.song.util;

import org.junit.Assert;
import org.junit.Test;
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
    public void duplicateExact_ambiguous_fallsBackToShortestTitle() {
        Song a = new Song();
        a.setTitle("Same");
        Song b = new Song();
        b.setTitle("Same");
        List<Song> list = List.of(a, b);
        ScheduleSongMatcher.Result r = ScheduleSongMatcher.match("Same", list);
        Assert.assertEquals(ScheduleSongMatcher.MatchStatus.AMBIGUOUS, r.status());
        Assert.assertEquals(2, r.alternatives().size());
        Assert.assertSame(a, r.song());
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
}
