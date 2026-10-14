package com.defensme.asset.api;

import com.defensme.asset.domain.AssetCriticality;
import com.defensme.asset.domain.AssetStatus;
import com.defensme.asset.domain.AssetType;
import com.defensme.asset.service.AssetService;
import com.defensme.auth.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    private static final Sort STABLE_SORT = Sort.by("name").ascending().and(Sort.by("id").ascending());

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_MANAGER', 'ANALYST', 'VIEWER')")
    public PageResponse<AssetResponse> search(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(required = false) @Size(max = 200) String search,
            @RequestParam(required = false) AssetType type,
            @RequestParam(required = false) AssetCriticality criticality,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return PageResponse.from(
                assetService.search(
                        principal.organizationId(), search, type, criticality, status,
                        PageRequest.of(page, size, STABLE_SORT)
                ),
                AssetResponse::from
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('IT_MANAGER', 'ANALYST')")
    public ResponseEntity<AssetResponse> create(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody CreateAssetRequest request
    ) {
        AssetResponse response = AssetResponse.from(assetService.create(
                principal.organizationId(), principal.userId(), request.toCommand()
        ));
        return ResponseEntity.created(URI.create("/api/v1/assets/" + response.id())).body(response);
    }

    @GetMapping("/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_MANAGER', 'ANALYST', 'VIEWER')")
    public AssetResponse get(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID assetId
    ) {
        return AssetResponse.from(assetService.get(principal.organizationId(), assetId));
    }

    @PutMapping("/{assetId}")
    @PreAuthorize("hasAnyRole('IT_MANAGER', 'ANALYST')")
    public AssetResponse update(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID assetId,
            @Valid @RequestBody UpdateAssetRequest request
    ) {
        return AssetResponse.from(assetService.update(
                principal.organizationId(), principal.userId(), assetId, request.toCommand()
        ));
    }

    @PostMapping("/{assetId}/archive")
    @PreAuthorize("hasRole('IT_MANAGER')")
    public ResponseEntity<Void> archive(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID assetId
    ) {
        assetService.archive(principal.organizationId(), principal.userId(), assetId);
        return ResponseEntity.noContent().build();
    }
}
