package me.dio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Refresh-token lifecycle:
 *   - login issues access + refresh
 *   - refresh rotates: old token is consumed, a new pair is issued
 *   - reusing a rotated refresh token is rejected (422)
 *   - logout revokes the refresh token
 *   - refreshing after logout is rejected
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthFlowTest {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult login() throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"devweekerson\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();
    }

    private String refreshOf(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return body.split("\"refreshToken\":\"")[1].split("\"")[0];
    }

    @Test
    void loginIssuesAccessAndRefreshTokens() throws Exception {
        login();
    }

    @Test
    void refreshRotatesTokenPair() throws Exception {
        MvcResult first = login();
        String oldRefresh = refreshOf(first);
        String oldAccess = first.getResponse().getContentAsString().split("\"token\":\"")[1].split("\"")[0];

        MvcResult second = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(oldRefresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        // New access token must differ from the old one (rotation).
        String newAccess = second.getResponse().getContentAsString().split("\"token\":\"")[1].split("\"\"")[0];
        org.assertj.core.api.Assertions.assertThat(newAccess).isNotEqualTo(oldAccess);
    }

    @Test
    void reusingRotatedRefreshTokenIsRejected() throws Exception {
        MvcResult first = login();
        String refresh = refreshOf(first);

        // First refresh consumes it.
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(refresh)))
                .andExpect(status().isOk());

        // Second use of the SAME token must be rejected (it was rotated).
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(refresh)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void logoutRevokesRefreshToken() throws Exception {
        MvcResult login = login();
        String refresh = refreshOf(login);

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(refresh)))
                .andExpect(status().isNoContent());

        // Refreshing with the logged-out token fails.
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"%s\"}".formatted(refresh)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void unknownRefreshTokenIsRejected() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"does-not-exist\"}"))
                .andExpect(status().isUnprocessableEntity());
    }
}
