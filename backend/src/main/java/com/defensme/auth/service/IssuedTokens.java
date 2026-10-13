package com.defensme.auth.service;

import java.time.Instant;

public record IssuedTokens(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
}
