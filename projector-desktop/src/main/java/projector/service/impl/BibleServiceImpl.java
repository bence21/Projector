package projector.service.impl;

import projector.model.Bible;
import projector.model.Language;
import projector.repository.DAOFactory;
import projector.repository.ormLite.VerseIndexRepositoryImpl;
import projector.service.BibleService;
import projector.service.ServiceException;
import projector.service.ServiceManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BibleServiceImpl extends AbstractBaseService<Bible> implements BibleService {
    private final VerseIndexRepositoryImpl verseIndexRepository;
    private final ConcurrentHashMap<Long, Bible> bibleHashMap = new ConcurrentHashMap<>();

    public BibleServiceImpl() {
        super(DAOFactory.getInstance().getBibleDAO());
        try {
            verseIndexRepository = new VerseIndexRepositoryImpl();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkHasVerseIndices(Bible bible) {
        if (bible == null || bible.hasVerseIndicesChecked()) {
            return;
        }
        bible.setHasVerseIndices(verseIndexRepository.countByBibleId(bible.getId()) > 0);
        bible.setHasVerseIndicesChecked(true);
    }

    @Override
    public void sort(List<Bible> bibles) {
        String songSelectedLanguageUuid = ServiceManager.getLanguageService().getSongSelectedLanguageUuid();
        bibles.sort((o2, o1) -> {
            Language o2Language = o2.getLanguage();
            Language o1Language = o1.getLanguage();
            if (o1Language != null && o2Language != null) {
                String o2LanguageUuid = o2Language.getUuid();
                String o1LanguageUuid = o1Language.getUuid();
                if (songSelectedLanguageUuid != null) {
                    if (o2LanguageUuid != null && o2LanguageUuid.equals(songSelectedLanguageUuid)) {
                        return -1;
                    }
                    if (o1LanguageUuid != null && o1LanguageUuid.equals(songSelectedLanguageUuid)) {
                        return 1;
                    }
                }
                int selectedCompare = Boolean.compare(o1Language.isSelected(), o2Language.isSelected());
                if (selectedCompare != 0) {
                    return selectedCompare;
                }
                if (o2LanguageUuid == null) {
                    if (o1LanguageUuid == null) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
                if (o1LanguageUuid == null) {
                    return -1;
                }
                return o2LanguageUuid.compareTo(o1LanguageUuid);
            }
            return 0;
        });
    }

    @Override
    public List<Bible> findAll() throws ServiceException {
        List<Bible> repositoryBibles = super.findAll();
        return getBiblesByHashMap(repositoryBibles);
    }

    private List<Bible> getBiblesByHashMap(List<Bible> repositoryBibles) {
        List<Bible> bibles = new ArrayList<>();
        for (Bible bible : repositoryBibles) {
            bibles.add(getByHashMap(bible));
        }
        return bibles;
    }

    private Bible getByHashMap(Bible bible) {
        if (bible == null) {
            return null;
        }
        Long id = bible.getId();
        if (id == null) {
            return bible;
        }
        if (bibleHashMap.containsKey(id)) {
            return bibleHashMap.get(id);
        }
        bibleHashMap.put(id, bible);
        return bible;
    }

    @Override
    public Bible create(Bible bible) throws ServiceException {
        return getByHashMap(super.create(bible));
    }

    @Override
    public List<Bible> create(List<Bible> bibles) throws ServiceException {
        return getBiblesByHashMap(super.create(bibles));
    }

    @Override
    public boolean delete(Bible bible) throws ServiceException {
        if (bible == null) {
            return false;
        }
        bibleHashMap.remove(bible.getId());
        return super.delete(bible);
    }

    @Override
    public boolean delete(List<Bible> bibles) throws ServiceException {
        for (Bible bible : bibles) {
            delete(bible);
        }
        return true;
    }

    @Override
    public Bible findByUuid(String uuid) {
        return getByHashMap(super.findByUuid(uuid));
    }

    @Override
    public Bible findById(Long id) {
        return getByHashMap(super.findById(id));
    }
}
