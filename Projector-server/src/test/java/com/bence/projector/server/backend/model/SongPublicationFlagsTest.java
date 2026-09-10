package com.bence.projector.server.backend.model;

import org.junit.Assert;
import org.junit.Test;

public class SongPublicationFlagsTest {

    @Test
    public void isPublic_falseWhenBlockingWordIssues() {
        Song song = baseVisibleSong();
        song.setHasBlockingWordIssues(true);
        Assert.assertFalse(song.isPublic());
    }

    @Test
    public void isPublic_trueWhenOnlyUnsolvedWordsWarning() {
        Song song = baseVisibleSong();
        song.setHasUnsolvedWords(true);
        song.setHasBlockingWordIssues(null);
        Assert.assertTrue(song.isPublic());
    }

    @Test
    public void isPublic_trueWhenNoBlockingFlags() {
        Song song = baseVisibleSong();
        song.setHasUnsolvedWords(null);
        song.setHasBlockingWordIssues(null);
        Assert.assertTrue(song.isPublic());
    }

    private static Song baseVisibleSong() {
        Song song = new Song();
        song.setDeleted(false);
        song.setReviewerErased(null);
        song.setIsBackUp(null);
        return song;
    }
}
