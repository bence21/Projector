package com.bence.projector.server.backend.service.inreview;

import com.bence.projector.server.backend.model.Song;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class InReviewSongPaginatorTest {

    @Test
    public void page_returnsSlice() {
        List<Song> songs = songs(5);
        List<Song> page = InReviewSongPaginator.page(songs, 1, 2);
        Assert.assertEquals(2, page.size());
        Assert.assertEquals("s2", page.get(0).getTitle());
        Assert.assertEquals("s3", page.get(1).getTitle());
    }

    @Test
    public void page_pastEnd_isEmpty() {
        Assert.assertTrue(InReviewSongPaginator.page(songs(2), 5, 10).isEmpty());
        Assert.assertTrue(InReviewSongPaginator.page(List.of(), 0, 10).isEmpty());
    }

    private static List<Song> songs(int n) {
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Song song = new Song();
            song.setTitle("s" + i);
            songs.add(song);
        }
        return songs;
    }
}
