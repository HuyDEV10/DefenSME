package com.defensme.identity.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByOrganizationIdAndEmail(UUID organizationId, String email);

    boolean existsByOrganizationIdAndEmail(UUID organizationId, String email);
}
