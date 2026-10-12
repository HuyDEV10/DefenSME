package com.defensme.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserAccountTest {

    @Test
    void createsActiveOrganizationScopedUserWithNormalizedEmail() {
        UUID organizationId = UUID.randomUUID();

        UserAccount user = UserAccount.create(
                organizationId,
                "  Analyst@Example.COM ",
                "$argon2id$test-hash",
                "Security Analyst"
        );

        assertThat(user.getOrganizationId()).isEqualTo(organizationId);
        assertThat(user.getEmail()).isEqualTo("analyst@example.com");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void rejectsMissingOrganizationScope() {
        assertThatThrownBy(() -> UserAccount.create(
                null,
                "analyst@example.com",
                "$argon2id$test-hash",
                "Security Analyst"
        )).isInstanceOf(NullPointerException.class)
                .hasMessage("organizationId is required");
    }
}
