package com.defensme.auth.security;

import com.defensme.auth.domain.AuthSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class SessionTokenValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_SESSION =
            new OAuth2Error("invalid_token", "Session is no longer active.", null);

    private final AuthSessionRepository sessionRepository;
    private final Clock clock;

    public SessionTokenValidator(AuthSessionRepository sessionRepository, Clock clock) {
        this.sessionRepository = sessionRepository;
        this.clock = clock;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        try {
            UUID sessionId = UUID.fromString(jwt.getClaimAsString("sid"));
            Instant now = clock.instant();
            if (sessionRepository.existsByIdAndRevokedAtIsNullAndExpiresAtAfter(sessionId, now)) {
                return OAuth2TokenValidatorResult.success();
            }
        } catch (IllegalArgumentException | NullPointerException exception) {
            // The same safe token failure is returned for missing and malformed session claims.
        }
        return OAuth2TokenValidatorResult.failure(INVALID_SESSION);
    }
}
