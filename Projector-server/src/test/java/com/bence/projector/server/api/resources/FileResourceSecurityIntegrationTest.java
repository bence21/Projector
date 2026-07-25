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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:projectorReleaseFileSecurity;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.task.scheduling.enabled=false"
})
public class FileResourceSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private MailSenderService mailSenderService;

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    public void user_blockedFromUploadReleaseFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "projector-setup.exe",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/deployer/api/projectorReleaseFile").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    public void admin_canUploadReleaseFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "projectorUpdate104.zip",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[]{4, 5, 6}
        );

        mockMvc.perform(multipart("/deployer/api/projectorReleaseFile").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("uploaded projectorUpdate104.zip"));

        Path targetFile = Paths.get(FileResource.PUBLIC_FOLDER, "projectorUpdate104.zip").toAbsolutePath().normalize();
        assertTrue(Files.exists(targetFile));
        Files.deleteIfExists(targetFile);
    }

    @Test
    @WithMockUser(username = "deployer@test.com", roles = "DEPLOYER")
    public void deployer_canUploadReleaseFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "projectorUpdate105.zip",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[]{7, 8, 9}
        );

        mockMvc.perform(multipart("/deployer/api/projectorReleaseFile").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("uploaded projectorUpdate105.zip"));

        Path targetFile = Paths.get(FileResource.PUBLIC_FOLDER, "projectorUpdate105.zip").toAbsolutePath().normalize();
        assertTrue(Files.exists(targetFile));
        Files.deleteIfExists(targetFile);
    }

    @Test
    @WithMockUser(username = "reviewer@test.com", roles = "REVIEWER")
    public void reviewer_blockedFromUploadReleaseFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "projector-setup.exe",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[]{1, 2, 3}
        );

        mockMvc.perform(multipart("/deployer/api/projectorReleaseFile").file(file))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    public void admin_rejectsInvalidReleaseFileName() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../secrets.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "nope".getBytes()
        );

        mockMvc.perform(multipart("/deployer/api/projectorReleaseFile").file(file))
                .andExpect(status().isBadRequest());
    }
}
