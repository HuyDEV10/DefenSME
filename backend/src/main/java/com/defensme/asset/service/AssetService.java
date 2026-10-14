package com.defensme.asset.service;

import static com.defensme.asset.domain.AssetSpecifications.criticalityIs;
import static com.defensme.asset.domain.AssetSpecifications.organizationIs;
import static com.defensme.asset.domain.AssetSpecifications.searchContains;
import static com.defensme.asset.domain.AssetSpecifications.statusIs;
import static com.defensme.asset.domain.AssetSpecifications.typeIs;

import com.defensme.asset.domain.Asset;
import com.defensme.asset.domain.AssetCriticality;
import com.defensme.asset.domain.AssetRepository;
import com.defensme.asset.domain.AssetStatus;
import com.defensme.asset.domain.AssetType;
import com.defensme.auth.service.AuthenticationAuditService;
import com.defensme.identity.domain.UserAccountRepository;
import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserAccountRepository userRepository;
    private final AuthenticationAuditService auditService;
    private final Clock clock;

    public AssetService(
            AssetRepository assetRepository,
            UserAccountRepository userRepository,
            AuthenticationAuditService auditService,
            Clock clock
    ) {
        this.assetRepository = assetRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<Asset> search(
            UUID organizationId,
            String search,
            AssetType type,
            AssetCriticality criticality,
            AssetStatus status,
            Pageable pageable
    ) {
        Specification<Asset> specification = Specification.where(organizationIs(organizationId))
                .and(searchContains(search))
                .and(typeIs(type))
                .and(criticalityIs(criticality))
                .and(statusIs(status));
        return assetRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public Asset get(UUID organizationId, UUID assetId) {
        return findScoped(organizationId, assetId);
    }

    @Transactional
    public Asset create(UUID organizationId, UUID actorId, AssetCommand command) {
        validateOwner(organizationId, command.ownerUserId());
        if (assetRepository.existsByOrganizationIdAndTypeAndIdentifierIgnoreCaseAndArchivedAtIsNull(
                organizationId,
                command.type(),
                command.identifier()
        )) {
            throw new AssetAlreadyExistsException();
        }
        Asset asset = Asset.create(
                organizationId,
                command.name(),
                command.type(),
                command.identifier(),
                command.criticality(),
                command.ownerUserId(),
                safeMetadata(command.metadata())
        );
        Asset saved = save(asset);
        auditService.record(organizationId, actorId, "ASSET_CREATED", "ASSET", saved.getId());
        return saved;
    }

    @Transactional
    public Asset update(UUID organizationId, UUID actorId, UUID assetId, AssetCommand command) {
        Asset asset = findScoped(organizationId, assetId);
        if (asset.getStatus() == AssetStatus.ARCHIVED) {
            throw new AssetStateException("Archived assets cannot be updated.");
        }
        if (command.status() == AssetStatus.ARCHIVED) {
            throw new AssetStateException("Use the archive operation to archive an asset.");
        }
        validateOwner(organizationId, command.ownerUserId());
        if (assetRepository.existsByOrganizationIdAndTypeAndIdentifierIgnoreCaseAndArchivedAtIsNullAndIdNot(
                organizationId,
                command.type(),
                command.identifier(),
                assetId
        )) {
            throw new AssetAlreadyExistsException();
        }
        asset.update(
                command.name(),
                command.type(),
                command.identifier(),
                command.criticality(),
                command.status(),
                command.ownerUserId(),
                safeMetadata(command.metadata())
        );
        Asset saved = save(asset);
        auditService.record(organizationId, actorId, "ASSET_UPDATED", "ASSET", saved.getId());
        return saved;
    }

    @Transactional
    public void archive(UUID organizationId, UUID actorId, UUID assetId) {
        Asset asset = findScoped(organizationId, assetId);
        if (asset.archive(clock.instant())) {
            assetRepository.saveAndFlush(asset);
            auditService.record(organizationId, actorId, "ASSET_ARCHIVED", "ASSET", asset.getId());
        }
    }

    private Asset findScoped(UUID organizationId, UUID assetId) {
        return assetRepository.findByOrganizationIdAndId(organizationId, assetId)
                .orElseThrow(AssetNotFoundException::new);
    }

    private void validateOwner(UUID organizationId, UUID ownerUserId) {
        if (ownerUserId != null && !userRepository.existsByIdAndOrganizationId(ownerUserId, organizationId)) {
            throw new AssetNotFoundException();
        }
    }

    private Map<String, Object> safeMetadata(Map<String, Object> metadata) {
        if (metadata != null && metadata.size() > 25) {
            throw new IllegalArgumentException("metadata must contain at most 25 entries");
        }
        return metadata == null ? Map.of() : metadata;
    }

    private Asset save(Asset asset) {
        try {
            return assetRepository.saveAndFlush(asset);
        } catch (DataIntegrityViolationException exception) {
            throw new AssetAlreadyExistsException();
        }
    }
}
