package projector.service;

import projector.model.Language;

import java.util.List;

public interface LanguageService extends CrudService<Language> {

    String getSongSelectedLanguageUuid();

    void sortLanguages(List<Language> languages);

    void setSongsSize(List<Language> languages);
}
