package com.defensme.asset.domain;

import com.defensme.core.domain.OrganizationScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "assets")
public class Asset extends OrganizationScopedEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssetType type;

    @Column(nullable = false, length = 200)
    private String identifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetCriticality criticality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssetStatus status;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "archived_at")
    private Instant archivedAt;

    protected Asset() {
    }

    private Asset(
            UUID organizationId,
            String name,
            AssetType type,
            String identifier,
            AssetCriticality criticality,
            UUID ownerUserId,
            Map<String, Object> metadata
    ) {
        super(organizationId);
        applyMutableFields(name, type, identifier, criticality, AssetStatus.ACTIVE, ownerUserId, metadata);
    }

    public static Asset create(
            UUID organizationId,
            String name,
            AssetType type,
            String identifier,
            AssetCriticality criticality,
            UUID ownerUserId,
            Map<String, Object> metadata
    ) {
        return new Asset(organizationId, name, type, identifier, criticality, ownerUserId, metadata);
    }

    public void update(
            String name,
            AssetType type,
            String identifier,
            AssetCriticality criticality,
            AssetStatus status,
            UUID ownerUserId,
            Map<String, Object> metadata
    ) {
        if (this.status == AssetStatus.ARCHIVED) {
            throw new IllegalStateException("Archived assets cannot be updated");
        }
        if (status == AssetStatus.ARCHIVED) {
            throw new IllegalArgumentException("Use archive operation for archived status");
        }
        applyMutableFields(name, type, identifier, criticality, status, ownerUserId, metadata);
    }

    public boolean archive(Instant occurredAt) {
        if (status != AssetStatus.ARCHIVED) {
            status = AssetStatus.ARCHIVED;
            archivedAt = occurredAt;
            return true;
        }
        return false;
    }

    private void applyMutableFields(
            String name,
            AssetType type,
            String identifier,
            AssetCriticality criticality,
            AssetStatus status,
            UUID ownerUserId,
            Map<String, Object> metadata
    ) {
        this.name = requireText(name, "name");
        this.type = java.util.Objects.requireNonNull(type, "type is required");
        this.identifier = requireText(identifier, "identifier");
        this.criticality = java.util.Objects.requireNonNull(criticality, "criticality is required");
        this.status = java.util.Objects.requireNonNull(status, "status is required");
        this.ownerUserId = ownerUserId;
        this.metadata = metadata == null ? new LinkedHashMap<>() : new LinkedHashMap<>(metadata);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    public String getName() { return name; }
    public AssetType getType() { return type; }
    public String getIdentifier() { return identifier; }
    public AssetCriticality getCriticality() { return criticality; }
    public AssetStatus getStatus() { return status; }
    public UUID getOwnerUserId() { return ownerUserId; }
    public Map<String, Object> getMetadata() { return java.util.Collections.unmodifiableMap(metadata); }
    public Instant getArchivedAt() { return archivedAt; }
}
