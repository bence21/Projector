package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.ProjectorVersionDTO;
import com.bence.projector.server.api.assembler.ProjectorVersionAssembler;
import com.bence.projector.server.backend.model.ProjectorVersion;
import com.bence.projector.server.backend.repository.ProjectorVersionRepository;
import com.bence.projector.server.backend.service.ProjectorVersionService;
import com.bence.projector.server.backend.service.StatisticsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ProjectorVersionResource.class})
@WebAppConfiguration
public class ProjectorVersionResourceTest {

    @Autowired
    private ProjectorVersionResource projectorVersionResource;
    @MockBean
    private ProjectorVersionService projectorVersionService;
    @MockBean
    private ProjectorVersionAssembler projectorVersionAssembler;
    @MockBean
    private StatisticsService statisticsService;
    @MockBean
    private ProjectorVersionRepository projectorVersionRepository;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
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

    @Test
    public void createProjectorVersion_returnsCreated() throws Exception {
        ProjectorVersionDTO requestDto = new ProjectorVersionDTO();
        requestDto.setVersion("3.7.7");
        requestDto.setVersionId(104);
        requestDto.setDescription("Bug fixes");

        ProjectorVersion model = new ProjectorVersion();
        model.setVersion("3.7.7");
        model.setVersionId(104);
        model.setDescription("Bug fixes");

        ProjectorVersion saved = new ProjectorVersion();
        saved.setVersion("3.7.7");
        saved.setVersionId(104);
        saved.setDescription("Bug fixes");

        ProjectorVersionDTO responseDto = new ProjectorVersionDTO();
        responseDto.setVersion("3.7.7");
        responseDto.setVersionId(104);
        responseDto.setDescription("Bug fixes");

        when(projectorVersionRepository.existsByVersionId(104)).thenReturn(false);
        when(projectorVersionAssembler.createModel(any(ProjectorVersionDTO.class))).thenReturn(model);
        when(projectorVersionService.save(model)).thenReturn(saved);
        when(projectorVersionAssembler.createDto(saved)).thenReturn(responseDto);

        mockMvc.perform(post("/deployer/api/projectorVersion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"3.7.7\",\"versionId\":104,\"description\":\"Bug fixes\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value("3.7.7"))
                .andExpect(jsonPath("$.versionId").value(104));

        verify(projectorVersionService).save(model);
    }

    @Test
    public void createProjectorVersion_duplicateVersionId_returnsConflict() throws Exception {
        when(projectorVersionRepository.existsByVersionId(104)).thenReturn(true);

        mockMvc.perform(post("/deployer/api/projectorVersion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"3.7.7\",\"versionId\":104}"))
                .andExpect(status().isConflict());
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