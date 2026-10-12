package com.defensme.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public abstract class OrganizationScopedEntity extends BaseEntity {

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    protected OrganizationScopedEntity() {
    }

    protected OrganizationScopedEntity(UUID organizationId) {
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
    }

    public UUID getOrganizationId() {
        return organizationId;
    }
}
