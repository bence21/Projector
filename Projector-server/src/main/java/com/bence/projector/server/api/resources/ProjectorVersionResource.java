package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.ProjectorVersionDTO;
import com.bence.projector.server.api.assembler.ProjectorVersionAssembler;
import com.bence.projector.server.backend.model.ProjectorVersion;
import com.bence.projector.server.backend.service.ProjectorVersionService;
import com.bence.projector.server.backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class ProjectorVersionResource {

    @Autowired
    private ProjectorVersionService projectorVersionService;
    @Autowired
    private ProjectorVersionAssembler projectorVersionAssembler;
    @Autowired
    private StatisticsService statisticsService;

    private List<ProjectorVersionDTO> findAllAfterDate_(HttpServletRequest httpServletRequest, List<ProjectorVersion> projectorVersions) {
        saveStatistics(httpServletRequest, statisticsService);
        return projectorVersionAssembler.createDtoList(projectorVersions);
    }

    private List<ProjectorVersionDTO> getOldAllAfterDate(HttpServletRequest httpServletRequest, int nr, int upperLimitExclusive) {
        return findAllAfterDate_(httpServletRequest, projectorVersionService.findAllAfterCreatedNrAndBeforeCreatedNr(nr, upperLimitExclusive));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/projectorVersionsAfterNr/{nr}")
    public List<ProjectorVersionDTO> findAllAfterDate(HttpServletRequest httpServletRequest, @PathVariable("nr") int nr) {
        return getOldAllAfterDate(httpServletRequest, nr, 40);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/projectorVersionsAfterNr/v3/{nr}")
    public List<ProjectorVersionDTO> findAllAfterDate_v3(HttpServletRequest httpServletRequest, @PathVariable("nr") int nr) {
        return getOldAllAfterDate(httpServletRequest, nr, 66);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/projectorVersionsAfterNr/v4/{nr}")
    public List<ProjectorVersionDTO> findAllAfterDate_v4(HttpServletRequest httpServletRequest, @PathVariable("nr") int nr) {
        return getOldAllAfterDate(httpServletRequest, nr, 92);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/projectorVersionsAfterNr/v5/{nr}")
    public List<ProjectorVersionDTO> findAllAfterDate_v5(HttpServletRequest httpServletRequest, @PathVariable("nr") int nr) {
        return findAllAfterDate_(httpServletRequest, projectorVersionService.findAllAfterCreatedNr(nr));
    }
}
