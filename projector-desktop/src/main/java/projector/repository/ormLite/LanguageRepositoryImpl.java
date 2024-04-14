package projector.repository.ormLite;

import projector.model.Language;
import projector.repository.LanguageRepository;

import java.sql.SQLException;

class LanguageRepositoryImpl extends AbstractBaseRepository<Language> implements LanguageRepository {

    LanguageRepositoryImpl() throws SQLException {
        super(Language.class, DatabaseHelper.getInstance().getLanguageDao().getDao());
    }
}
