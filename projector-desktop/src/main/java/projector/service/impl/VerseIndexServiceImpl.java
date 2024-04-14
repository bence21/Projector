package projector.service.impl;

import projector.model.BibleVerse;
import projector.model.VerseIndex;
import projector.repository.DAOFactory;
import projector.repository.VerseIndexRepository;
import projector.service.VerseIndexService;

import java.util.List;

public class VerseIndexServiceImpl extends AbstractService<VerseIndex> implements VerseIndexService {

    private final VerseIndexRepository verseIndexDAO = DAOFactory.getInstance().getVerseIndexDAO();

    public VerseIndexServiceImpl() {
        super(DAOFactory.getInstance().getVerseIndexDAO());
    }

    @Override
    public List<VerseIndex> findByIndex(Long index) {
        return verseIndexDAO.findByIndex(index);
    }

    @Override
    public List<BibleVerse> findByIndexAndBibleId(Long index, Long bibleId) {
        return verseIndexDAO.findByIndexAndBibleId(index, bibleId);
    }
}
