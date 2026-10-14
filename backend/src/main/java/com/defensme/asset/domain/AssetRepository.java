package com.defensme.asset.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetRepository extends JpaRepository<Asset, UUID>, JpaSpecificationExecutor<Asset> {

    Optional<Asset> findByOrganizationIdAndId(UUID organizationId, UUID id);

    boolean existsByOrganizationIdAndTypeAndIdentifierIgnoreCaseAndArchivedAtIsNull(
            UUID organizationId,
            AssetType type,
            String identifier
    );

    boolean existsByOrganizationIdAndTypeAndIdentifierIgnoreCaseAndArchivedAtIsNullAndIdNot(
            UUID organizationId,
            AssetType type,
            String identifier,
            UUID id
    );
}
