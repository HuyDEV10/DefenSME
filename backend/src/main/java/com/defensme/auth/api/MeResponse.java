package com.defensme.auth.api;

import com.defensme.auth.security.AuthenticatedUser;
import com.defensme.identity.domain.UserAccount;
import java.util.List;
import java.util.UUID;

public record MeResponse(
        UUID id,
        UUID organizationId,
        String email,
        String displayName,
        List<String> roles
) {
    public static MeResponse from(UserAccount user, AuthenticatedUser principal) {
        return new MeResponse(
                user.getId(),
                user.getOrganizationId(),
                user.getEmail(),
                user.getDisplayName(),
                principal.roles()
        );
    }
}
