package projector.service.impl;

import projector.application.Settings;
import projector.model.Language;
import projector.repository.DAOFactory;
import projector.service.LanguageService;
import projector.service.ServiceManager;
import projector.service.SongService;

import java.util.List;

public class LanguageServiceImpl extends AbstractBaseService<Language> implements LanguageService {

    public LanguageServiceImpl() {
        super(DAOFactory.getInstance().getLanguageDAO());
    }

    @Override
    public String getSongSelectedLanguageUuid() {
        Language songSelectedLanguage = Settings.getInstance().getSongSelectedLanguage();
        if (songSelectedLanguage == null) {
            return null;
        }
        return songSelectedLanguage.getUuid();
    }

    @Override
    public void sortLanguages(List<Language> languages) {
        String songSelectedLanguageUuid = getSongSelectedLanguageUuid();
        SongService songService = ServiceManager.getSongService();
        setSongsSize(languages);
        languages.sort((o2, o1) -> {
            if (songSelectedLanguageUuid != null) {
                if (o2.getUuid().equals(songSelectedLanguageUuid)) {
                    return -1;
                }
                if (o1.getUuid().equals(songSelectedLanguageUuid)) {
                    return 1;
                }
            }
            int selectedCompare = Boolean.compare(o1.isSelected(), o2.isSelected());
            if (selectedCompare != 0) {
                return selectedCompare;
            }
            return Long.compare(o1.getSongsSize(songService), o2.getSongsSize(songService));
        });
    }

    @Override
    public void setSongsSize(List<Language> languages) {
        SongService songService = ServiceManager.getSongService();
        for (Language language : languages) {
            language.setSongsSize(songService.countByLanguage(language));
        }
    }

    @Override
    public Language findByUuid(String uuid) {
        Language language = super.findByUuid(uuid);
        if (language == null) {
            return null;
        }
        language.setSongsSize(ServiceManager.getSongService().countByLanguage(language));
        return language;
    }
}
