package com.defensme.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_roles")
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Column(name = "assigned_by")
    private UUID assignedBy;

    protected UserRole() {
    }

    private UserRole(UserRoleId id, UUID assignedBy) {
        this.id = id;
        this.assignedBy = assignedBy;
        this.assignedAt = Instant.now();
    }

    public static UserRole assign(UUID userId, Short roleId, UUID assignedBy) {
        return new UserRole(new UserRoleId(userId, roleId), assignedBy);
    }

    public UserRoleId getId() {
        return id;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public UUID getAssignedBy() {
        return assignedBy;
    }
}
