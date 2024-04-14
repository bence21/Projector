package projector.repository;

import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import projector.model.Song;

import java.util.List;

public interface SongDAO extends CrudDAO<Song> {
    Song findByTitle(String title);

    List<Song> findAllByVersionGroup(String versionGroup);

    void saveViews(List<SongViewsDTO> songViewsDTOS);

    void saveFavouriteCount(List<SongFavouritesDTO> songFavouritesDTOS);
}
