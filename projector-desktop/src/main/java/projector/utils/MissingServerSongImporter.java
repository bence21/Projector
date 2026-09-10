package projector.utils;

import projector.model.Song;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MissingServerSongImporter {

    private MissingServerSongImporter() {
    }

    public static List<Song> findMissingServerSongs(List<Song> localSongs, List<Song> serverSongs) {
        if (serverSongs == null || serverSongs.isEmpty()) {
            return List.of();
        }
        Set<String> localUuids = new HashSet<>();
        if (localSongs != null) {
            for (Song song : localSongs) {
                String uuid = song.getUuid();
                if (uuid != null) {
                    localUuids.add(uuid);
                }
            }
        }
        List<Song> missing = new ArrayList<>();
        for (Song serverSong : serverSongs) {
            String uuid = serverSong.getUuid();
            if (uuid != null && !localUuids.contains(uuid) && !serverSong.isDeleted()) {
                missing.add(serverSong);
            }
        }
        return missing;
    }
}
