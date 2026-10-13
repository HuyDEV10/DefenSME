package com.defensme.auth.domain;

import com.defensme.core.domain.OrganizationScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "auth_sessions")
public class AuthSession extends OrganizationScopedEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64, updatable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "rotated_from", updatable = false)
    private UUID rotatedFrom;

    protected AuthSession() {
    }

    private AuthSession(
            UUID organizationId,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            UUID rotatedFrom
    ) {
        super(organizationId);
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash is required");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt is required");
        this.rotatedFrom = rotatedFrom;
    }

    public static AuthSession issue(
            UUID organizationId,
            UUID userId,
            String tokenHash,
            Instant expiresAt,
            UUID rotatedFrom
    ) {
        return new AuthSession(organizationId, userId, tokenHash, expiresAt, rotatedFrom);
    }

    public boolean isActiveAt(Instant instant) {
        return revokedAt == null && expiresAt.isAfter(instant);
    }

    public void revoke(Instant occurredAt) {
        if (revokedAt == null) {
            revokedAt = occurredAt;
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public UUID getRotatedFrom() {
        return rotatedFrom;
    }
}
