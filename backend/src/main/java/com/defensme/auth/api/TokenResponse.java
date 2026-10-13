package com.defensme.auth.api;

import com.defensme.auth.service.AuthResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt,
        UserIdentity user
) {
    public static TokenResponse from(AuthResult result) {
        return new TokenResponse(
                "Bearer",
                result.tokens().accessToken(),
                result.tokens().refreshToken(),
                result.tokens().accessTokenExpiresAt(),
                result.tokens().refreshTokenExpiresAt(),
                new UserIdentity(
                        result.userId(),
                        result.organizationId(),
                        result.email(),
                        result.displayName(),
                        result.roles()
                )
        );
    }

    public record UserIdentity(
            UUID id,
            UUID organizationId,
            String email,
            String displayName,
            List<String> roles
    ) {
    }
}
