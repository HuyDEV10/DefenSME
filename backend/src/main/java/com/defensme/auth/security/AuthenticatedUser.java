package com.defensme.auth.security;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(
        UUID userId,
        UUID organizationId,
        UUID sessionId,
        List<String> roles
) {
}
