package com.defensme.identity.api;

import com.defensme.auth.security.AuthenticatedUser;
import com.defensme.identity.domain.RoleRepository;
import com.defensme.identity.domain.UserAccountRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;

    public UserController(UserAccountRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserSummaryResponse> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        return userRepository.findAllByOrganizationIdOrderByDisplayNameAsc(principal.organizationId()).stream()
                .map(user -> UserSummaryResponse.from(
                        user,
                        roleRepository.findRoleCodesByUserId(user.getId())
                ))
                .toList();
    }
}
