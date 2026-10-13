package com.defensme.identity.api;

import com.defensme.identity.domain.UserAccount;
import java.util.List;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String email,
        String displayName,
        String status,
        List<String> roles
) {
    public static UserSummaryResponse from(UserAccount user, List<String> roles) {
        return new UserSummaryResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getStatus().name(),
                List.copyOf(roles)
        );
    }
}
