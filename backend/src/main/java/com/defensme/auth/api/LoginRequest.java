package com.defensme.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record LoginRequest(
        @NotNull UUID organizationId,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 200) String password
) {
}
