package com.bence.songbook.repository.impl.ormLite;

import android.util.Log;

import com.bence.songbook.models.BaseEntity;
import com.bence.songbook.repository.BaseRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

public abstract class AbstractRepository<T extends BaseEntity> implements BaseRepository<T> {
    private static final String TAG = AbstractRepository.class.getSimpleName();
    private final String clazzName;
    private Dao<T, Long> dao;
    private final String clazzNames;

    AbstractRepository(Class<T> clazz) {
        clazzName = clazz.getSimpleName().toLowerCase();
        clazzNames = clazzName + "s";
    }

    @Override
    public T findOne(final Long id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            String msg = "Could not find " + clazzName;
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<T> findAll() {
        try {
            return dao.queryForAll();
        } catch (final SQLException e) {
            String msg = "Could not find all " + clazzNames;
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(final T model) {
        try {
            dao.createOrUpdate(model);
        } catch (SQLException e) {
            String msg = "Could not save " + clazzName;
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(final List<T> models) {
        try {
            for (final T model : models) {
                dao.createOrUpdate(model);
            }
        } catch (SQLException e) {
            String msg = "Could not save " + clazzNames;
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void delete(final T t) {
        try {
            dao.delete(t);
        } catch (SQLException e) {
            String msg = "Could not delete " + clazzName;
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void deleteAll(final List<T> ts) {
        try {
            for (final T t : ts) {
                dao.delete(t);
            }
        } catch (SQLException e) {
            String msg = "Could not delete all " + clazzNames;
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    void setDao(CustomDao<T, Long> dao) {
        this.dao = dao.getDao();
    }
}
