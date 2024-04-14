package projector.service;

import projector.model.Language;
import projector.model.SongCollection;

import java.util.List;

public interface SongCollectionService extends CrudService<SongCollection> {
    List<SongCollection> findAllByLanguage(Language language);
}
