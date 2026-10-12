package com.defensme.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UserRoleId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_id", nullable = false)
    private Short roleId;

    protected UserRoleId() {
    }

    public UserRoleId(UUID userId, Short roleId) {
        this.userId = Objects.requireNonNull(userId, "userId is required");
        this.roleId = Objects.requireNonNull(roleId, "roleId is required");
    }

    public UUID getUserId() {
        return userId;
    }

    public Short getRoleId() {
        return roleId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserRoleId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}
