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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies Spring Security rejects cross-role access on song admin/reviewer URL prefixes.
 * Allowed-path behavior (admin/reviewer using {@code /user/api/song}) is covered in
 * {@link SongResourceCreateUpdateTest}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:songResourceSecurity;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true",
        "spring.task.scheduling.enabled=false"
})
public class SongResourceSecurityIntegrationTest {

    private static final String SONG_ID = "sec-song-1";

    @Autowired
    private MockMvc mockMvc;

    /**
     * Prevents startup {@code CreateAdmin} from querying reserved H2 table name {@code user}.
     */
    @MockBean
    private UserService userService;

    @MockBean
    private MailSenderService mailSenderService;

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    public void user_blockedFromAdminUpdate() throws Exception {
        mockMvc.perform(put("/admin/api/song/{songId}", SONG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Sec\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "reviewer@test.com", roles = "REVIEWER")
    public void reviewer_blockedFromAdminUpdate() throws Exception {
        mockMvc.perform(put("/admin/api/song/{songId}", SONG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Sec\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    public void user_blockedFromReviewerUpdate() throws Exception {
        mockMvc.perform(put("/reviewer/api/song/{songId}", SONG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Sec\"}"))
                .andExpect(status().isForbidden());
    }
}
