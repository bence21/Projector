package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.BaseEntity;
import com.bence.projector.server.backend.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public abstract class BaseServiceImpl<M extends BaseEntity> implements BaseService<M> {
    @Autowired
    private CrudRepository<M, Long> repository;

    @Override
    public M findOne(Long id) {
        Optional<M> optional = repository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public List<M> findAll() {
        return (List<M>) repository.findAll();
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void delete(final List<Long> ids) {
        ids.forEach(id -> repository.deleteById(id));
    }

    @Override
    public void deleteAll(List<M> models) {
        for (M book : models) {
            delete(book.getId());
        }
    }

    @Override
    public M save(final M model) {
        return repository.save(model);
    }

    @Override
    public Iterable<M> save(final List<M> models) {
        if (models == null) {
            return null;
        }
        return repository.saveAll(models);
    }

    @Override
    public void saveAllByRepository(List<M> models) {
        save(models);
    }
}
