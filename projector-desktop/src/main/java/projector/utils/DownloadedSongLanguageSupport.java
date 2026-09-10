package projector.utils;

import projector.model.Language;
import projector.model.Song;

import java.util.List;

public final class DownloadedSongLanguageSupport {

    private DownloadedSongLanguageSupport() {
    }

    public static void attachLanguage(List<Song> songs, Language language) {
        if (songs == null || language == null) {
            return;
        }
        for (Song song : songs) {
            song.setLanguage(language);
        }
    }
}
