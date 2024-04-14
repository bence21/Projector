package projector.service;

import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import projector.model.Language;
import projector.model.Song;

import java.util.List;

public interface SongService extends CrudService<Song> {
    Song findByTitle(String title);

    List<Song> findAllByVersionGroup(String versionGroup);

    void saveViews(List<SongViewsDTO> songViewsDTOS);

    void saveFavouriteCount(List<SongFavouritesDTO> songFavouritesDTOS);

    Song getFromMemoryOrSong(Song song);

    Song getFromMemoryOrSongNoUpdate(Song song);

    long countByLanguage(Language language);
}
