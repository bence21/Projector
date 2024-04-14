package projector.repository;

import projector.model.Language;
import projector.model.SongCollection;

import java.util.List;

public interface SongCollectionRepository extends CrudDAO<SongCollection> {
    List<SongCollection> findAllByLanguage(Language language);
}
