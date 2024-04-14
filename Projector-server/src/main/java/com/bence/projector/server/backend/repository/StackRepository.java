package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Stack;
import org.springframework.data.repository.CrudRepository;

public interface StackRepository extends CrudRepository<Stack, Long> {
    Stack findByStackTrace(String stackTrace);

    Stack findOneByUuid(String uuid);
}
