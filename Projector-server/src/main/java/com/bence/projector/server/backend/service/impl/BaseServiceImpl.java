package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.BaseEntity;
import com.bence.projector.server.backend.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public abstract class BaseServiceImpl<M extends BaseEntity> implements BaseService<M> {
    @Autowired
    private CrudRepository<M, Long> repository;

    @Override
    public M findOne(Long id) {
        return repository.findOne(id);
    }

    @Override
    public List<M> findAll() {
        return (List<M>) repository.findAll();
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }

    @Override
    public void delete(final List<Long> ids) {
        ids.forEach(id -> repository.delete(id));
    }

    @Override
    public M save(final M model) {
        return repository.save(model);
    }

    @Override
    public Iterable<M> save(final List<M> models) {
        return repository.save(models);
    }

    @Override
    public M findOneByUuid(String uuid) {
        return null;
    }

    @Override
    public void deleteByUuid(String uuid) {
    }
}
