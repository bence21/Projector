package projector.service;

import projector.model.BibleVerse;
import projector.model.VerseIndex;

import java.util.List;

public interface VerseIndexService extends CrudService<VerseIndex> {
    List<VerseIndex> findByIndex(Long index);

    List<BibleVerse> findByIndexAndBibleId(Long index, Long bibleId);
}
