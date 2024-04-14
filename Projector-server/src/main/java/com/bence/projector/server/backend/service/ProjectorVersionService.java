package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.ProjectorVersion;

import java.util.List;

public interface ProjectorVersionService extends BaseService<ProjectorVersion> {
    List<ProjectorVersion> findAllAfterCreatedNr(int nr);

    List<ProjectorVersion> findAllAfterCreatedNrAndBeforeCreatedNr(int nr, int nr2);
}
