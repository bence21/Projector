package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.ProjectorVersion;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectorVersionRepository extends CrudRepository<ProjectorVersion, Long> {

    List<ProjectorVersion> findAllByVersionIdGreaterThan(int nr);

    List<ProjectorVersion> findAllByVersionIdGreaterThanAndVersionIdLessThan(int nr, int nr2);
}
