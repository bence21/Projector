package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Stack;
import com.bence.projector.server.backend.repository.StackRepository;
import com.bence.projector.server.backend.service.StackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StackServiceImpl extends BaseServiceImpl<Stack> implements StackService {
    private final StackRepository stackRepository;

    @Autowired
    public StackServiceImpl(StackRepository stackRepository) {
        this.stackRepository = stackRepository;
    }

    @Override
    public Stack findByStackTrace(String stackTrace) {
        return stackRepository.findByStackTrace(stackTrace);
    }

    @Override
    public Stack findOneByUuid(String uuid) {
        return stackRepository.findOneByUuid(uuid);
    }
}
