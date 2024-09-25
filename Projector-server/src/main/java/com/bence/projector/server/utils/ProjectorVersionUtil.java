package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.ProjectorVersion;
import com.bence.projector.server.backend.repository.ProjectorVersionRepository;

import java.util.Date;

@SuppressWarnings("unused")
public class ProjectorVersionUtil {
    public static void createNewProjectorVersion(ProjectorVersionRepository projectorVersionRepository) {
        ProjectorVersion projectorVersion = new ProjectorVersion();
        projectorVersion.setVersion("3.5.10");
        projectorVersion.setCreatedDate(new Date());
        projectorVersion.setDescription("Progress bar for song slides. Performance improvements and bug fixes.");
        projectorVersion.setVersionId(84);
        projectorVersionRepository.save(projectorVersion);
    }
}
