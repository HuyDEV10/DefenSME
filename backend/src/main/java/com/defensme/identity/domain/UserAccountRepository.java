package com.defensme.identity.domain;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByOrganizationIdAndEmail(UUID organizationId, String email);

    Optional<UserAccount> findByOrganizationIdAndEmailIgnoreCase(UUID organizationId, String email);

    boolean existsByOrganizationIdAndEmail(UUID organizationId, String email);

    List<UserAccount> findAllByOrganizationIdOrderByDisplayNameAsc(UUID organizationId);
}
