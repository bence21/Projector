package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.ProjectorVersionDTO;
import com.bence.projector.server.backend.model.ProjectorVersion;
import org.springframework.stereotype.Component;

@Component
public class ProjectorVersionAssembler implements GeneralAssembler<ProjectorVersion, ProjectorVersionDTO> {

    @Override
    public ProjectorVersionDTO createDto(ProjectorVersion projectorVersion) {
        if (projectorVersion == null) {
            return null;
        }
        ProjectorVersionDTO projectorVersionDTO = new ProjectorVersionDTO();
        projectorVersionDTO.setCreatedDate(projectorVersion.getCreatedDate());
        projectorVersionDTO.setDescription(projectorVersion.getDescription());
        projectorVersionDTO.setVersion(projectorVersion.getVersion());
        projectorVersionDTO.setVersionId(projectorVersion.getVersionId());
        projectorVersionDTO.setUuid(projectorVersion.getUuid());
        return projectorVersionDTO;
    }

    @Override
    public ProjectorVersion createModel(ProjectorVersionDTO projectorVersionDTO) {
        return updateModel(new ProjectorVersion(), projectorVersionDTO);
    }

    @Override
    public ProjectorVersion updateModel(ProjectorVersion projectorVersion, ProjectorVersionDTO projectorVersionDTO) {
        if (projectorVersion != null) {
            projectorVersion.setCreatedDate(projectorVersionDTO.getCreatedDate());
            projectorVersion.setDescription(projectorVersionDTO.getDescription());
            projectorVersion.setVersion(projectorVersionDTO.getVersion());
            projectorVersion.setVersionId(projectorVersionDTO.getVersionId());
        }
        return projectorVersion;
    }

}
