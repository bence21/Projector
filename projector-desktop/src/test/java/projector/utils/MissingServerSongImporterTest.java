package projector.utils;

import org.junit.Assert;
import org.junit.Test;
import projector.model.Song;

import java.util.List;

public class MissingServerSongImporterTest {

    @Test
    public void findMissingServerSongs_returnsServerOnlySongs() {
        Song local = song("local-uuid");
        Song serverMatch = song("local-uuid");
        Song serverOnly = song("server-only-uuid");

        List<Song> missing = MissingServerSongImporter.findMissingServerSongs(
                List.of(local), List.of(serverMatch, serverOnly));

        Assert.assertEquals(1, missing.size());
        Assert.assertEquals("server-only-uuid", missing.get(0).getUuid());
    }

    @Test
    public void findMissingServerSongs_ignoresDeletedServerSongs() {
        Song serverOnly = song("server-only-uuid");
        serverOnly.setDeleted(true);

        List<Song> missing = MissingServerSongImporter.findMissingServerSongs(
                List.of(), List.of(serverOnly));

        Assert.assertTrue(missing.isEmpty());
    }

    private static Song song(String uuid) {
        Song song = new Song();
        song.setUuid(uuid);
        return song;
    }
}
