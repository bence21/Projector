package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Stack;

public interface StackService extends BaseService<Stack> {
    Stack findByStackTrace(String stackTrace);

    Stack findOneByUuid(String uuid);
}
