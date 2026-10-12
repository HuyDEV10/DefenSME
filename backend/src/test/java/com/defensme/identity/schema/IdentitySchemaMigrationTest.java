package com.defensme.identity.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class IdentitySchemaMigrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("defensme")
                    .withUsername("defensme")
                    .withPassword("defensme-test");

    @Test
    void migratesEmptyDatabaseAndSeedsDefaultRoles() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .load();

        MigrateResult migration = flyway.migrate();

        assertThat(migration.success).isTrue();
        assertThat(migration.migrationsExecuted).isEqualTo(2);
        assertThat(tableExists("organizations")).isTrue();
        assertThat(tableExists("users")).isTrue();
        assertThat(tableExists("roles")).isTrue();
        assertThat(tableExists("user_roles")).isTrue();
        assertThat(roleCodes()).containsExactly("ADMIN", "ANALYST", "IT_MANAGER", "VIEWER");
    }

    private boolean tableExists(String tableName) throws Exception {
        String sql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables "
                + "WHERE table_schema = 'public' AND table_name = ?)";
        try (Connection connection = POSTGRES.createConnection("");
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        }
    }

    private List<String> roleCodes() throws Exception {
        try (Connection connection = POSTGRES.createConnection("");
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT code FROM roles ORDER BY code");
             ResultSet result = statement.executeQuery()) {
            var codes = new java.util.ArrayList<String>();
            while (result.next()) {
                codes.add(result.getString(1));
            }
            return codes;
        }
    }
}
