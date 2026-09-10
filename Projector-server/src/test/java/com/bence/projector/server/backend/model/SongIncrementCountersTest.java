package com.bence.projector.server.backend.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * Tests counter fields and defensive copies on accessors used by view/favourite increment flows.
 */
public class SongIncrementCountersTest {

    @Test
    public void incrementViews_incrementsPrimitive() {
        Song song = new Song();
        song.setViews(4);
        song.incrementViews();
        Assert.assertEquals(5, song.getViews());
    }

    @Test
    public void incrementFavourites_incrementsPrimitive() {
        Song song = new Song();
        song.setFavourites(2);
        song.incrementFavourites();
        Assert.assertEquals(3, song.getFavourites());
    }

    @Test
    public void getLastIncrementViewDate_returnsDefensiveClone() {
        Song song = new Song();
        Date d = new Date(12345);
        song.setLastIncrementViewDate(d);
        Date out = song.getLastIncrementViewDate();
        Assert.assertEquals(d.getTime(), out.getTime());
        out.setTime(99999);
        Assert.assertEquals(d.getTime(), song.getLastIncrementViewDate().getTime());
    }

    @Test
    public void getLastIncrementFavouritesDate_returnsDefensiveClone() {
        Song song = new Song();
        Date d = new Date(22222);
        song.setLastIncrementFavouritesDate(d);
        Date out = song.getLastIncrementFavouritesDate();
        Assert.assertEquals(d.getTime(), out.getTime());
        out.setTime(77777);
        Assert.assertEquals(d.getTime(), song.getLastIncrementFavouritesDate().getTime());
    }
}
