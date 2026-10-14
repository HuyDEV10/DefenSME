package com.defensme.asset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
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
class AssetIntegrationTest {

    private static final String PASSWORD = "Correct-Horse-Battery-Staple-42";
    private static final UUID ORG_A = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ORG_B = UUID.fromString("10000000-0000-0000-0000-000000000002");
    private static final UUID MANAGER_A = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID ANALYST_A = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID VIEWER_A = UUID.fromString("20000000-0000-0000-0000-000000000003");
    private static final UUID MANAGER_B = UUID.fromString("20000000-0000-0000-0000-000000000004");

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("defensme")
            .withUsername("defensme")
            .withPassword("defensme-test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedIdentityData() {
        jdbcTemplate.update("DELETE FROM audit_events");
        jdbcTemplate.update("DELETE FROM assets");
        jdbcTemplate.update("DELETE FROM auth_sessions");
        jdbcTemplate.update("DELETE FROM user_roles");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM organizations");

        insertOrganization(ORG_A, "Primary SME");
        insertOrganization(ORG_B, "Other SME");
        insertUser(MANAGER_A, ORG_A, "manager@primary.test", "Primary Manager");
        insertUser(ANALYST_A, ORG_A, "analyst@primary.test", "Primary Analyst");
        insertUser(VIEWER_A, ORG_A, "viewer@primary.test", "Primary Viewer");
        insertUser(MANAGER_B, ORG_B, "manager@other.test", "Other Manager");
        assignRole(MANAGER_A, (short) 2);
        assignRole(ANALYST_A, (short) 3);
        assignRole(VIEWER_A, (short) 4);
        assignRole(MANAGER_B, (short) 2);
    }

    @Test
    void managerCanCreateSearchUpdateAndArchiveAssets() throws Exception {
        String token = login(ORG_A, "manager@primary.test");
        MvcResult created = mockMvc.perform(post("/api/v1/assets")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(createBody("Web Server", "SERVER", "srv-001", "CRITICAL", MANAGER_A)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/v1/assets/.+")))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.metadata.environment").value("production"))
                .andReturn();
        String assetId = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asText();

        mockMvc.perform(get("/api/v1/assets")
                        .header("Authorization", bearer(token))
                        .param("search", "server")
                        .param("type", "SERVER")
                        .param("criticality", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(put("/api/v1/assets/{id}", assetId)
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(updateBody("Public Web Server", "SERVER", "srv-001", "HIGH", "INACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Public Web Server"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(post("/api/v1/assets/{id}/archive", assetId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/assets")
                        .header("Authorization", bearer(token))
                        .param("status", "ARCHIVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(assetId))
                .andExpect(jsonPath("$.content[0].archivedAt").isNotEmpty());
    }

    @Test
    void analystCanWriteButCannotArchiveAndViewerIsReadOnly() throws Exception {
        String analystToken = login(ORG_A, "analyst@primary.test");
        MvcResult created = mockMvc.perform(post("/api/v1/assets")
                        .header("Authorization", bearer(analystToken))
                        .contentType("application/json")
                        .content(createBody("Database", "DATABASE", "db-001", "HIGH", ANALYST_A)))
                .andExpect(status().isCreated())
                .andReturn();
        String assetId = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asText();
        mockMvc.perform(post("/api/v1/assets/{id}/archive", assetId)
                        .header("Authorization", bearer(analystToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        String viewerToken = login(ORG_A, "viewer@primary.test");
        mockMvc.perform(get("/api/v1/assets/{id}", assetId)
                        .header("Authorization", bearer(viewerToken)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/assets")
                        .header("Authorization", bearer(viewerToken))
                        .contentType("application/json")
                        .content(createBody("Denied", "SERVER", "denied", "LOW", null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void everyIdLookupIsScopedToAuthenticatedOrganization() throws Exception {
        UUID foreignAsset = insertAsset(ORG_B, MANAGER_B, "Foreign Server", "foreign-001");
        String token = login(ORG_A, "manager@primary.test");

        mockMvc.perform(get("/api/v1/assets/{id}", foreignAsset).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
        mockMvc.perform(put("/api/v1/assets/{id}", foreignAsset)
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(updateBody("Foreign", "SERVER", "foreign-001", "LOW", "ACTIVE")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/assets/{id}/archive", foreignAsset)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void activeIdentifierIsUniquePerOrganizationAndReusableAfterArchive() throws Exception {
        String token = login(ORG_A, "manager@primary.test");
        MvcResult created = mockMvc.perform(post("/api/v1/assets")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(createBody("Server One", "SERVER", "SRV-UNIQUE", "HIGH", null)))
                .andExpect(status().isCreated())
                .andReturn();
        String assetId = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asText();

        mockMvc.perform(post("/api/v1/assets")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(createBody("Duplicate", "SERVER", "srv-unique", "LOW", null)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_ALREADY_EXISTS"));

        mockMvc.perform(post("/api/v1/assets/{id}/archive", assetId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/v1/assets")
                        .header("Authorization", bearer(token))
                        .contentType("application/json")
                        .content(createBody("Replacement", "SERVER", "srv-unique", "HIGH", null)))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidPaginationAndEnumUseProblemContract() throws Exception {
        String token = login(ORG_A, "manager@primary.test");
        mockMvc.perform(get("/api/v1/assets")
                        .header("Authorization", bearer(token))
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        mockMvc.perform(get("/api/v1/assets")
                        .header("Authorization", bearer(token))
                        .param("type", "ROUTER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    private String login(UUID organizationId, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "organizationId", organizationId,
                                "email", email,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("accessToken").asText();
    }

    private String createBody(String name, String type, String identifier, String criticality, UUID ownerId)
            throws Exception {
        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("name", name);
        body.put("type", type);
        body.put("identifier", identifier);
        body.put("criticality", criticality);
        if (ownerId != null) body.put("ownerUserId", ownerId);
        body.put("metadata", Map.of("environment", "production"));
        return objectMapper.writeValueAsString(body);
    }

    private String updateBody(String name, String type, String identifier, String criticality, String assetStatus)
            throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "name", name,
                "type", type,
                "identifier", identifier,
                "criticality", criticality,
                "status", assetStatus,
                "metadata", Map.of("environment", "production")
        ));
    }

    private UUID insertAsset(UUID organizationId, UUID ownerId, String name, String identifier) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO assets (id, organization_id, name, type, identifier, criticality, status, owner_user_id)
                VALUES (?, ?, ?, 'SERVER', ?, 'HIGH', 'ACTIVE', ?)
                """, id, organizationId, name, identifier, ownerId);
        return id;
    }

    private void insertOrganization(UUID id, String name) {
        jdbcTemplate.update("INSERT INTO organizations (id, name) VALUES (?, ?)", id, name);
    }

    private void insertUser(UUID id, UUID organizationId, String email, String displayName) {
        jdbcTemplate.update("""
                INSERT INTO users (id, organization_id, email, password_hash, display_name, status)
                VALUES (?, ?, ?, ?, ?, 'ACTIVE')
                """, id, organizationId, email, passwordEncoder.encode(PASSWORD), displayName);
    }

    private void assignRole(UUID userId, short roleId) {
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", userId, roleId);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
