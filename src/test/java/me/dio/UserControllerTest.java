package me.dio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for the user endpoints: the /users/check uniqueness binding
 * and the @Valid bean validation on POST /users. The service layer is covered
 * by UserServiceTest; this covers the HTTP binding (params, 422, 201).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // roll back created users so the shared H2 seed stays intact
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void checkUniqueness_reportsSeedAccountTaken() throws Exception {
        // V5 seed: account '0002' exists.
        mockMvc.perform(get("/users/check").param("accountNumber", "0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumberAvailable").value(false))
                .andExpect(jsonPath("$.cardNumberAvailable").value(true));
    }

    @Test
    void checkUniqueness_reportsFreeNumberAvailable() throws Exception {
        mockMvc.perform(get("/users/check").param("accountNumber", "999-unused"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumberAvailable").value(true));
    }

    @Test
    void checkUniqueness_ignoresOwnIdOnEdit() throws Exception {
        // Account '0002' belongs to user 2; excluding id=2 makes it "available".
        mockMvc.perform(get("/users/check")
                        .param("accountNumber", "0002")
                        .param("excludeId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumberAvailable").value(true));
    }

    @Test
    void checkUniqueness_noParamsIsAvailable() throws Exception {
        mockMvc.perform(get("/users/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumberAvailable").value(true))
                .andExpect(jsonPath("$.cardNumberAvailable").value(true));
    }

    @Test
    void createUser_blankNameIs422() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "   ",
                                  "account": {"number": "900001", "agency": "0001", "balance": 0, "limit": 0},
                                  "card": {"number": "**** **** **** 9000", "limit": 0},
                                  "features": [],
                                  "news": []
                                }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createUser_nullFeaturesIs422() throws Exception {
        // @NotNull on features/news: null is rejected by bean validation.
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Novo",
                                  "account": {"number": "900002", "agency": "0001", "balance": 0, "limit": 0},
                                  "card": {"number": "**** **** **** 9001", "limit": 0},
                                  "features": null,
                                  "news": null
                                }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void createUser_validIs201() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Novo Valido",
                                  "account": {"number": "900003", "agency": "0001", "balance": 0, "limit": 0},
                                  "card": {"number": "**** **** **** 9002", "limit": 0},
                                  "features": [],
                                  "news": []
                                }
                                """))
                .andExpect(status().isCreated());
    }
}
