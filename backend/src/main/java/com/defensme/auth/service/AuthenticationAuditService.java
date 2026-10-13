package com.defensme.auth.service;

import java.time.Clock;
import java.sql.Timestamp;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationAuditService {

    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

    public AuthenticationAuditService(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID organizationId, UUID actorId, String eventType, String resourceType, UUID resourceId) {
        jdbcTemplate.update("""
                INSERT INTO audit_events (
                    id, organization_id, actor_id, event_type,
                    resource_type, resource_id, event_data, occurred_at
                ) VALUES (?, ?, ?, ?, ?, ?, CAST(? AS jsonb), ?)
                """,
                UUID.randomUUID(),
                organizationId,
                actorId,
                eventType,
                resourceType,
                resourceId,
                "{}",
                Timestamp.from(clock.instant())
        );
    }
}
