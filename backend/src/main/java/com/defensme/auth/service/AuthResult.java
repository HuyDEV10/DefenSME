package com.defensme.auth.service;

import java.util.List;
import java.util.UUID;

public record AuthResult(
        IssuedTokens tokens,
        UUID userId,
        UUID organizationId,
        String email,
        String displayName,
        List<String> roles
) {
}
