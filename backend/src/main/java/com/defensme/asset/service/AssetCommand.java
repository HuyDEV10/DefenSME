package com.defensme.asset.service;

import com.defensme.asset.domain.AssetCriticality;
import com.defensme.asset.domain.AssetStatus;
import com.defensme.asset.domain.AssetType;
import java.util.Map;
import java.util.UUID;

public record AssetCommand(
        String name,
        AssetType type,
        String identifier,
        AssetCriticality criticality,
        AssetStatus status,
        UUID ownerUserId,
        Map<String, Object> metadata
) {
}
