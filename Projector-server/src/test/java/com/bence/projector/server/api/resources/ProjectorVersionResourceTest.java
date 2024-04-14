package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.ProjectorVersionDTO;
import com.bence.projector.server.api.assembler.ProjectorVersionAssembler;
import com.bence.projector.server.backend.model.ProjectorVersion;
import com.bence.projector.server.backend.service.ProjectorVersionService;
import com.bence.projector.server.backend.service.StatisticsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ProjectorVersionResource.class})
@WebAppConfiguration
public class ProjectorVersionResourceTest {

    @InjectMocks
    private ProjectorVersionResource projectorVersionResource;
    @MockBean
    private ProjectorVersionService projectorVersionService;
    @MockBean
    private ProjectorVersionAssembler projectorVersionAssembler;
    @MockBean
    private StatisticsService statisticsService;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(projectorVersionResource).build();
    }

    @Test
    public void testFindAllAfterDate() throws Exception {
        List<ProjectorVersion> projectorVersions = new ArrayList<>();
        ProjectorVersion projectorVersion = getATestProjectorVersion();
        projectorVersions.add(projectorVersion);
        int nr = 1;
        when(projectorVersionService.findAllAfterCreatedNrAndBeforeCreatedNr(nr, 40)).thenReturn(projectorVersions);
        List<ProjectorVersionDTO> projectorVersionsDTOS = new ArrayList<>();
        ProjectorVersionDTO projectorVersionDTO = getProjectorVersionDTO(projectorVersion);
        projectorVersionsDTOS.add(projectorVersionDTO);
        when(projectorVersionAssembler.createDtoList(projectorVersions)).thenReturn(projectorVersionsDTOS);
        String urlTemplate = "/api/projectorVersionsAfterNr/{nr}";
        mockMvc.perform(get(urlTemplate, nr))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    private ProjectorVersionDTO getProjectorVersionDTO(ProjectorVersion projectorVersion) {
        ProjectorVersionDTO projectorVersionDTO = new ProjectorVersionDTO();
        projectorVersionDTO.setVersion(projectorVersion.getVersion());
        return projectorVersionDTO;
    }

    private ProjectorVersion getATestProjectorVersion() {
        ProjectorVersion projectorVersion = new ProjectorVersion();
        projectorVersion.setVersion("test");
        return projectorVersion;
    }
}