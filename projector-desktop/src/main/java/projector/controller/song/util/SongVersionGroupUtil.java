package projector.controller.song.util;

import projector.model.Song;
import projector.service.ServiceManager;
import projector.service.SongService;

import java.util.List;

public final class SongVersionGroupUtil {

    private SongVersionGroupUtil() {
    }

    public static String getVersionGroupOrUuid(Song song) {
        if (song == null) {
            return null;
        }
        String versionGroup = song.getVersionGroup();
        if (versionGroup == null) {
            return song.getUuid();
        }
        return versionGroup;
    }

    public static List<Song> getVersionAlternatives(Song song) {
        String versionGroup = getVersionGroupOrUuid(song);
        if (versionGroup == null) {
            return List.of();
        }
        SongService songService = ServiceManager.getSongService();
        if (songService == null) {
            return List.of();
        }
        return songService.findAllByVersionGroup(versionGroup);
    }

    public static boolean hasVersionAlternatives(Song song) {
        return getVersionAlternatives(song).size() > 1;
    }
}
