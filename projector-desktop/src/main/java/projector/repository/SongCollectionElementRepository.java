package projector.repository;

import projector.model.Song;
import projector.model.SongCollection;
import projector.model.SongCollectionElement;

import java.util.List;

public interface SongCollectionElementRepository extends CrudDAO<SongCollectionElement> {
    List<SongCollectionElement> findBySong(Song song);

    long countBySongCollection(SongCollection songCollection);
}
