package com.defensme.asset.api;

import com.defensme.asset.domain.Asset;
import com.defensme.asset.domain.AssetCriticality;
import com.defensme.asset.domain.AssetStatus;
import com.defensme.asset.domain.AssetType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AssetResponse(
        UUID id,
        UUID organizationId,
        String name,
        AssetType type,
        String identifier,
        AssetCriticality criticality,
        AssetStatus status,
        UUID ownerUserId,
        Map<String, Object> metadata,
        Instant archivedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static AssetResponse from(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getOrganizationId(),
                asset.getName(),
                asset.getType(),
                asset.getIdentifier(),
                asset.getCriticality(),
                asset.getStatus(),
                asset.getOwnerUserId(),
                asset.getMetadata(),
                asset.getArchivedAt(),
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }
}
