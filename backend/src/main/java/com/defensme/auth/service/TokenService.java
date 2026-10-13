package com.defensme.auth.service;

import com.defensme.auth.config.AuthProperties;
import com.defensme.auth.domain.AuthSession;
import com.defensme.auth.domain.AuthSessionRepository;
import com.defensme.identity.domain.UserAccount;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final AuthSessionRepository sessionRepository;
    private final AuthProperties properties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenService(
            JwtEncoder jwtEncoder,
            AuthSessionRepository sessionRepository,
            AuthProperties properties,
            Clock clock
    ) {
        this.jwtEncoder = jwtEncoder;
        this.sessionRepository = sessionRepository;
        this.properties = properties;
        this.clock = clock;
    }

    public IssuedTokens issue(UserAccount user, List<String> roles, UUID rotatedFrom) {
        Instant now = clock.instant();
        Instant refreshExpiresAt = now.plus(properties.refreshTokenTtl());
        String refreshToken = newRefreshToken();
        AuthSession session = sessionRepository.saveAndFlush(AuthSession.issue(
                user.getOrganizationId(),
                user.getId(),
                hashRefreshToken(refreshToken),
                refreshExpiresAt,
                rotatedFrom
        ));

        Instant accessExpiresAt = now.plus(properties.accessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("defensme-backend")
                .issuedAt(now)
                .expiresAt(accessExpiresAt)
                .subject(user.getId().toString())
                .claim("org", user.getOrganizationId().toString())
                .claim("sid", session.getId().toString())
                .claim("roles", roles)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new IssuedTokens(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt);
    }

    public String hashRefreshToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
