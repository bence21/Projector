package com.bence.projector.server.api.resources;

import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.mailsending.MailSenderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:projectorVersionSecurity;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.task.scheduling.enabled=false"
})
public class ProjectorVersionResourceSecurityIntegrationTest {

    private static final String CREATE_VERSION_BODY = "{\"version\":\"3.7.7\",\"versionId\":104}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private MailSenderService mailSenderService;

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    public void user_blockedFromCreateProjectorVersion() throws Exception {
        mockMvc.perform(post("/deployer/api/projectorVersion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_VERSION_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "reviewer@test.com", roles = "REVIEWER")
    public void reviewer_blockedFromCreateProjectorVersion() throws Exception {
        mockMvc.perform(post("/deployer/api/projectorVersion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_VERSION_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "deployer@test.com", roles = "DEPLOYER")
    public void deployer_canCreateProjectorVersion() throws Exception {
        mockMvc.perform(post("/deployer/api/projectorVersion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_VERSION_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    public void admin_canCreateProjectorVersion() throws Exception {
        mockMvc.perform(post("/deployer/api/projectorVersion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"3.7.8\",\"versionId\":105}"))
                .andExpect(status().isCreated());
    }
}
