package projector.service;

import projector.model.Bible;

import java.util.List;

public interface BibleService extends CrudService<Bible> {
    void checkHasVerseIndices(Bible bible);

    void sort(List<Bible> bibles);
}
