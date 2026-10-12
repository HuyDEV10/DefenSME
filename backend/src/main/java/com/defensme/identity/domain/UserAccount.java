package com.defensme.identity.domain;

import com.defensme.core.domain.OrganizationScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserAccount extends OrganizationScopedEntity {

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected UserAccount() {
    }

    private UserAccount(UUID organizationId, String email, String passwordHash, String displayName) {
        super(organizationId);
        this.email = normalizeEmail(email);
        this.passwordHash = requireText(passwordHash, "passwordHash");
        this.displayName = requireText(displayName, "displayName");
        this.status = UserStatus.ACTIVE;
    }

    public static UserAccount create(
            UUID organizationId,
            String email,
            String passwordHash,
            String displayName
    ) {
        return new UserAccount(organizationId, email, passwordHash, displayName);
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    private static String normalizeEmail(String value) {
        return requireText(value, "email").toLowerCase(Locale.ROOT);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
