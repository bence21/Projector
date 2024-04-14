package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.Language;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.LanguageRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LanguageRepositoryImpl extends AbstractRepository<Language> implements LanguageRepository {
    private static final String TAG = LanguageRepositoryImpl.class.getSimpleName();

    public LanguageRepositoryImpl(Context context) {
        super(Language.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            CustomDao<Language, Long> languageDao = databaseHelper.getLanguageDao();
            super.setDao(languageDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize LanguageRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(Language model) {
        super.save(model);
    }

    @Override
    public void save(List<Language> languages) {
        for (Language language : languages) {
            save(language);
        }
    }

    @Override
    public List<Language> findAllSelectedForDownload() {
        List<Language> languages = findAll();
        List<Language> filtered = new ArrayList<>(languages.size());
        for (Language language : languages) {
            if (language.isSelectedForDownload()) {
                filtered.add(language);
            }
        }
        return filtered;
    }
}
