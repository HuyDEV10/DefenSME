package com.defensme.asset.api;

import com.defensme.asset.domain.AssetCriticality;
import com.defensme.asset.domain.AssetType;
import com.defensme.asset.service.AssetCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;

public record CreateAssetRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull AssetType type,
        @NotBlank @Size(max = 200) String identifier,
        @NotNull AssetCriticality criticality,
        UUID ownerUserId,
        @Size(max = 25) Map<String, Object> metadata
) {
    AssetCommand toCommand() {
        return new AssetCommand(name, type, identifier, criticality, null, ownerUserId, metadata);
    }
}
