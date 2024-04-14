package projector.repository;

import projector.model.BibleVerse;
import projector.model.VerseIndex;

import java.util.List;

public interface VerseIndexRepository extends CrudDAO<VerseIndex> {
    List<VerseIndex> findByIndex(Long index);

    List<BibleVerse> findByIndexAndBibleId(Long index, Long bibleId);

    long countByBibleId(Long bibleId);
}
