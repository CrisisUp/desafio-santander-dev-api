package me.dio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ownership/authorization rules:
 *   - ADMIN (devweekerson) may read/update any user and any account.
 *   - USER (ana, linked to banking user 2 / account 2) may only access their own
 *     user (id 2) and account (id 2); everything else is 403.
 *   - /accounts/transactions/summary is ADMIN-only.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OwnershipTest {

    @Autowired
    private MockMvc mockMvc;

    private String adminToken;
    private String anaToken;

    @BeforeEach
    void login() throws Exception {
        adminToken = login("devweekerson", "admin123");
        anaToken = login("ana", "senha123");
    }

    private String login(String username, String password) throws Exception {
        MvcResult res = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        String body = res.getResponse().getContentAsString();
        return body.split("\"token\":\"")[1].split("\"")[0];
    }

    // ---- GET /users/{id} ----

    @Test
    void adminCanReadAnyUser() throws Exception {
        mockMvc.perform(get("/users/2").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users/5").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void userCanReadOwnUser() throws Exception {
        mockMvc.perform(get("/users/2").header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotReadAnotherUser() throws Exception {
        mockMvc.perform(get("/users/5").header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isForbidden());
    }

    // ---- PUT /users/{id} ----

    @Test
    void userCannotUpdateAnotherUser() throws Exception {
        mockMvc.perform(put("/users/5")
                        .header("Authorization", "Bearer " + anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 5,
                                  "name": "Diego Rocha",
                                  "account": {"id": 5, "number": "0005", "agency": "0001", "balance": 0, "limit": 0},
                                  "card": {"id": 5, "number": "**** **** **** 2204", "limit": 0},
                                  "features": [],
                                  "news": []
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanUpdateOwnUser() throws Exception {
        mockMvc.perform(put("/users/2")
                        .header("Authorization", "Bearer " + anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 2,
                                  "name": "Ana Souza",
                                  "account": {"id": 2, "number": "0002", "agency": "0001", "balance": 780.50, "limit": 1000},
                                  "card": {"id": 2, "number": "**** **** **** 2201", "limit": 3000},
                                  "features": [{"id": 6, "description": "PIX"}, {"id": 7, "description": "Pagar"}],
                                  "news": [{"id": 3, "description": "O Santander tem soluções de crédito sob medida pra você. Confira!"}]
                                }
                                """))
                .andExpect(status().isOk());
    }

    // ---- GET /accounts/{id}/transactions ----

    @Test
    void userCanReadOwnAccountStatement() throws Exception {
        mockMvc.perform(get("/accounts/2/transactions")
                        .header("Authorization", "Bearer " + anaToken)
                        .param("page", "0").param("size", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotReadAnotherAccountStatement() throws Exception {
        mockMvc.perform(get("/accounts/5/transactions")
                        .header("Authorization", "Bearer " + anaToken)
                        .param("page", "0").param("size", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadAnyAccountStatement() throws Exception {
        mockMvc.perform(get("/accounts/5/transactions")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0").param("size", "5"))
                .andExpect(status().isOk());
    }

    // ---- POST /accounts/{id}/transactions ----

    @Test
    void userCannotPostToAnotherAccount() throws Exception {
        mockMvc.perform(post("/accounts/5/transactions")
                        .header("Authorization", "Bearer " + anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DEPOSIT\",\"amount\":10.00}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanPostToOwnAccount() throws Exception {
        mockMvc.perform(post("/accounts/2/transactions")
                        .header("Authorization", "Bearer " + anaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DEPOSIT\",\"amount\":10.00}"))
                .andExpect(status().isCreated());
    }

    // ---- GET /accounts/transactions/summary (ADMIN only) ----

    @Test
    void summaryRequiresAdmin() throws Exception {
        mockMvc.perform(get("/accounts/transactions/summary").header("Authorization", "Bearer " + anaToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/accounts/transactions/summary").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
