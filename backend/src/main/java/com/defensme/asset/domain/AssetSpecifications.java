package com.defensme.asset.domain;

import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class AssetSpecifications {

    private AssetSpecifications() {
    }

    public static Specification<Asset> organizationIs(UUID organizationId) {
        return (root, query, builder) -> builder.equal(root.get("organizationId"), organizationId);
    }

    public static Specification<Asset> searchContains(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("name")), pattern),
                builder.like(builder.lower(root.get("identifier")), pattern)
        );
    }

    public static Specification<Asset> typeIs(AssetType type) {
        return type == null ? null : (root, query, builder) -> builder.equal(root.get("type"), type);
    }

    public static Specification<Asset> criticalityIs(AssetCriticality criticality) {
        return criticality == null
                ? null
                : (root, query, builder) -> builder.equal(root.get("criticality"), criticality);
    }

    public static Specification<Asset> statusIs(AssetStatus status) {
        return status == null ? null : (root, query, builder) -> builder.equal(root.get("status"), status);
    }
}
