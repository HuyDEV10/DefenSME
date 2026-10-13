package com.defensme.auth.service;

import com.defensme.auth.domain.AuthSession;
import com.defensme.auth.domain.AuthSessionRepository;
import com.defensme.identity.domain.RoleRepository;
import com.defensme.identity.domain.UserAccount;
import com.defensme.identity.domain.UserAccountRepository;
import com.defensme.identity.domain.UserStatus;
import com.defensme.organization.domain.OrganizationRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizationRepository organizationRepository;
    private final AuthSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationAuditService auditService;
    private final Clock clock;
    private final String dummyPasswordHash;

    public AuthService(
            UserAccountRepository userRepository,
            RoleRepository roleRepository,
            OrganizationRepository organizationRepository,
            AuthSessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            AuthenticationAuditService auditService,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.organizationRepository = organizationRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.auditService = auditService;
        this.clock = clock;
        this.dummyPasswordHash = passwordEncoder.encode("defensme-dummy-password");
    }

    @Transactional
    public AuthResult login(UUID organizationId, String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        UserAccount user = userRepository
                .findByOrganizationIdAndEmailIgnoreCase(organizationId, normalizedEmail)
                .orElse(null);

        String candidateHash = user == null ? dummyPasswordHash : user.getPasswordHash();
        boolean passwordMatches = passwordEncoder.matches(password, candidateHash);
        if (user == null || !passwordMatches || user.getStatus() != UserStatus.ACTIVE) {
            recordFailedLoginWhenOrganizationExists(organizationId, user);
            throw new InvalidCredentialsException();
        }

        List<String> roles = roleRepository.findRoleCodesByUserId(user.getId());
        Instant now = clock.instant();
        user.recordSuccessfulLogin(now);
        IssuedTokens tokens = tokenService.issue(user, roles, null);
        auditService.record(organizationId, user.getId(), "AUTH_LOGIN_SUCCEEDED", "USER", user.getId());
        return result(user, roles, tokens);
    }

    @Transactional
    public AuthResult refresh(String refreshToken) {
        Instant now = clock.instant();
        AuthSession session = sessionRepository
                .findByTokenHashAndRevokedAtIsNull(tokenService.hashRefreshToken(refreshToken))
                .filter(candidate -> candidate.isActiveAt(now))
                .orElseThrow(InvalidCredentialsException::new);
        UserAccount user = userRepository.findById(session.getUserId())
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(InvalidCredentialsException::new);

        session.revoke(now);
        List<String> roles = roleRepository.findRoleCodesByUserId(user.getId());
        IssuedTokens tokens = tokenService.issue(user, roles, session.getId());
        auditService.record(
                user.getOrganizationId(),
                user.getId(),
                "AUTH_TOKEN_REFRESHED",
                "AUTH_SESSION",
                session.getId()
        );
        return result(user, roles, tokens);
    }

    @Transactional
    public void logout(String refreshToken, UUID authenticatedUserId) {
        sessionRepository.findByTokenHashAndRevokedAtIsNull(tokenService.hashRefreshToken(refreshToken))
                .filter(session -> session.getUserId().equals(authenticatedUserId))
                .ifPresent(session -> {
                    session.revoke(clock.instant());
                    auditService.record(
                            session.getOrganizationId(),
                            authenticatedUserId,
                            "AUTH_LOGOUT",
                            "AUTH_SESSION",
                            session.getId()
                    );
                });
    }

    private AuthResult result(UserAccount user, List<String> roles, IssuedTokens tokens) {
        return new AuthResult(
                tokens,
                user.getId(),
                user.getOrganizationId(),
                user.getEmail(),
                user.getDisplayName(),
                List.copyOf(roles)
        );
    }

    private void recordFailedLoginWhenOrganizationExists(UUID organizationId, UserAccount user) {
        if (organizationRepository.existsById(organizationId)) {
            auditService.record(
                    organizationId,
                    user == null ? null : user.getId(),
                    "AUTH_LOGIN_FAILED",
                    "USER",
                    user == null ? null : user.getId()
            );
        }
    }
}
