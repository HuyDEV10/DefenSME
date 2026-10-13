package com.defensme.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
        "app.auth.jwt-secret=test-secret-with-at-least-32-characters",
        "app.auth.access-token-ttl=PT15M",
        "app.auth.refresh-token-ttl=P7D"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthenticationIntegrationTest {

    private static final String PASSWORD = "Correct-Horse-Battery-Staple-42";
    private static final UUID ORGANIZATION_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID SECOND_ORGANIZATION_ID = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID ADMIN_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID ANALYST_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("defensme")
                    .withUsername("defensme")
                    .withPassword("defensme-test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedIdentityData() {
        jdbcTemplate.update("DELETE FROM audit_events");
        jdbcTemplate.update("DELETE FROM auth_sessions");
        jdbcTemplate.update("DELETE FROM user_roles");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM organizations");

        insertOrganization(ORGANIZATION_ID, "Primary SME");
        insertOrganization(SECOND_ORGANIZATION_ID, "Other SME");
        insertUser(ADMIN_ID, ORGANIZATION_ID, "admin@primary.test", "Primary Admin", "ACTIVE");
        insertUser(ANALYST_ID, ORGANIZATION_ID, "analyst@primary.test", "Primary Analyst", "ACTIVE");
        insertUser(UUID.randomUUID(), SECOND_ORGANIZATION_ID, "admin@other.test", "Other Admin", "ACTIVE");
        assignRole(ADMIN_ID, (short) 1);
        assignRole(ANALYST_ID, (short) 3);
    }

    @Test
    void validLoginReturnsCredentialsAndAuthenticatedIdentity() throws Exception {
        JsonNode login = login("admin@primary.test", PASSWORD, status().isOk());
        String accessToken = login.path("accessToken").asText();

        assertThat(accessToken).isNotBlank();
        assertThat(login.path("refreshToken").asText()).isNotBlank();
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ADMIN_ID.toString()))
                .andExpect(jsonPath("$.organizationId").value(ORGANIZATION_ID.toString()))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    void invalidCredentialsAndMissingTokenFailWithoutAccountDisclosure() throws Exception {
        MvcResult wrongPassword = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody("admin@primary.test", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andReturn();

        MvcResult unknownUser = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody("unknown@primary.test", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andReturn();

        assertThat(objectMapper.readTree(wrongPassword.getResponse().getContentAsString()).path("detail").asText())
                .isEqualTo(objectMapper.readTree(unknownUser.getResponse().getContentAsString()).path("detail").asText());
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void roleAuthorizationDeniesAnalystAndScopesAdminListToOrganization() throws Exception {
        String analystToken = login("analyst@primary.test", PASSWORD, status().isOk())
                .path("accessToken").asText();
        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + analystToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        String adminToken = login("admin@primary.test", PASSWORD, status().isOk())
                .path("accessToken").asText();
        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("admin@primary.test"))
                .andExpect(jsonPath("$[1].email").value("analyst@primary.test"));
    }

    @Test
    void refreshRotatesSessionAndLogoutRevokesNewSession() throws Exception {
        JsonNode first = login("admin@primary.test", PASSWORD, status().isOk());
        String firstAccess = first.path("accessToken").asText();
        String firstRefresh = first.path("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new TokenBody(firstRefresh))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode second = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        String secondAccess = second.path("accessToken").asText();
        String secondRefresh = second.path("refreshToken").asText();

        assertThat(secondRefresh).isNotEqualTo(firstRefresh);
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + firstAccess))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + secondAccess)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new TokenBody(secondRefresh))))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + secondAccess))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode login(String email, String password, org.springframework.test.web.servlet.ResultMatcher status)
            throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(loginBody(email, password)))
                .andExpect(status)
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String loginBody(String email, String password) throws Exception {
        return objectMapper.writeValueAsString(new LoginBody(ORGANIZATION_ID, email, password));
    }

    private void insertOrganization(UUID id, String name) {
        jdbcTemplate.update(
                "INSERT INTO organizations (id, name) VALUES (?, ?)",
                id,
                name
        );
    }

    private void insertUser(UUID id, UUID organizationId, String email, String displayName, String userStatus) {
        jdbcTemplate.update("""
                INSERT INTO users (id, organization_id, email, password_hash, display_name, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                id,
                organizationId,
                email,
                passwordEncoder.encode(PASSWORD),
                displayName,
                userStatus
        );
    }

    private void assignRole(UUID userId, short roleId) {
        jdbcTemplate.update(
                "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)",
                userId,
                roleId
        );
    }

    private record LoginBody(UUID organizationId, String email, String password) {
    }

    private record TokenBody(String refreshToken) {
    }
}
