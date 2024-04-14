package com.bence.songbook.repository;

import java.util.List;

public interface BaseRepository<T> {

    T findOne(final Long id);

    List<T> findAll();

    void save(final T model);

    void save(final List<T> model);

    void delete(final T t);

    void deleteAll(final List<T> ts);
}

