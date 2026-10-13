package com.defensme.auth.api;

import com.defensme.auth.security.AuthenticatedUser;
import com.defensme.auth.service.AuthService;
import com.defensme.identity.domain.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final UserAccountRepository userRepository;

    public AuthController(AuthService authService, UserAccountRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.from(authService.login(
                request.organizationId(),
                request.email(),
                request.password()
        ));
    }

    @PostMapping("/auth/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return TokenResponse.from(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        authService.logout(request.refreshToken(), principal.userId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return userRepository.findById(principal.userId())
                .filter(user -> user.getOrganizationId().equals(principal.organizationId()))
                .map(user -> MeResponse.from(user, principal))
                .orElseThrow(InvalidAuthenticatedUserException::new);
    }
}
