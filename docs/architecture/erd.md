# DefenSME logical data model

**Status:** C01 implementation baseline  
**Database:** PostgreSQL 17  
**Migration authority:** Flyway

This is the logical target model for the course MVP. The existing `organizations` and `audit_events` tables remain valid; later commits add the remaining tables through incremental migrations. JPA must use `ddl-auto=validate` and must not generate production schema.

```mermaid
erDiagram
    ORGANIZATIONS ||--o{ USERS : contains
    ORGANIZATIONS ||--o{ ASSETS : owns
    ORGANIZATIONS ||--o{ ALERTS : receives
    ORGANIZATIONS ||--o{ INCIDENTS : manages
    ORGANIZATIONS ||--o{ AUDIT_EVENTS : records
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : grants
    ASSETS ||--o{ ALERTS : affected_by
    ALERTS ||--o{ ALERT_EVIDENCE : includes
    ALERTS ||--o{ ALERT_STATUS_HISTORY : changes
    ALERTS ||--o{ AI_ANALYSES : analyzed_by
    AI_ANALYSES ||--o| AI_REVIEWS : reviewed_as
    ALERTS ||--o| INCIDENTS : escalates_to
    USERS ||--o{ INCIDENTS : owns
    INCIDENTS ||--o{ INCIDENT_ACTIONS : contains
    USERS ||--o{ INCIDENT_ACTIONS : performs

    ORGANIZATIONS {
        uuid id PK
        varchar name
        timestamptz created_at
        timestamptz updated_at
    }
    USERS {
        uuid id PK
        uuid organization_id FK
        varchar email
        varchar password_hash
        varchar display_name
        varchar status
        timestamptz last_login_at
        timestamptz created_at
        timestamptz updated_at
    }
    ROLES {
        smallint id PK
        varchar code UK
        varchar name
    }
    USER_ROLES {
        uuid user_id PK,FK
        smallint role_id PK,FK
        timestamptz assigned_at
        uuid assigned_by FK
    }
    ASSETS {
        uuid id PK
        uuid organization_id FK
        varchar name
        varchar type
        varchar identifier
        varchar criticality
        varchar status
        uuid owner_user_id FK
        jsonb metadata
        timestamptz archived_at
        timestamptz created_at
        timestamptz updated_at
    }
    ALERTS {
        uuid id PK
        uuid organization_id FK
        uuid asset_id FK
        varchar title
        text description
        varchar source
        varchar observed_severity
        varchar approved_severity
        varchar status
        timestamptz observed_at
        uuid assigned_to FK
        timestamptz created_at
        timestamptz updated_at
    }
    ALERT_EVIDENCE {
        uuid id PK
        uuid organization_id FK
        uuid alert_id FK
        varchar evidence_type
        text content
        varchar storage_reference
        varchar checksum
        timestamptz created_at
    }
    ALERT_STATUS_HISTORY {
        uuid id PK
        uuid organization_id FK
        uuid alert_id FK
        varchar previous_status
        varchar new_status
        uuid changed_by FK
        text reason
        timestamptz changed_at
    }
    AI_ANALYSES {
        uuid id PK
        uuid organization_id FK
        uuid alert_id FK
        varchar schema_version
        varchar provider
        varchar model
        varchar prompt_version
        jsonb normalized_input
        jsonb original_output
        varchar status
        varchar recommended_severity
        numeric confidence
        text failure_code
        timestamptz created_at
    }
    AI_REVIEWS {
        uuid id PK
        uuid organization_id FK
        uuid analysis_id FK,UK
        uuid reviewer_id FK
        varchar decision
        jsonb approved_output
        text reason
        timestamptz reviewed_at
    }
    INCIDENTS {
        uuid id PK
        uuid organization_id FK
        uuid alert_id FK,UK
        varchar title
        varchar status
        varchar priority
        uuid owner_id FK
        timestamptz opened_at
        timestamptz resolved_at
        timestamptz closed_at
        timestamptz updated_at
    }
    INCIDENT_ACTIONS {
        uuid id PK
        uuid organization_id FK
        uuid incident_id FK
        uuid actor_id FK
        varchar action_type
        text description
        jsonb evidence_refs
        timestamptz occurred_at
    }
    AUDIT_EVENTS {
        uuid id PK
        uuid organization_id FK
        uuid actor_id
        varchar event_type
        varchar resource_type
        uuid resource_id
        jsonb event_data
        varchar correlation_id
        timestamptz occurred_at
    }
```

## Required invariants

1. Every operational row carries `organization_id`; repository queries and service authorization must match it to the authenticated organization.
2. Emails are unique within an organization using a normalized lower-case value.
3. Asset identifiers are unique within `(organization_id, type, identifier)` when not archived.
4. Alert state is one of `NEW`, `TRIAGED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`; transitions are enforced by the backend, not database clients.
5. `observed_severity`, AI `recommended_severity`, and human `approved_severity` remain separate.
6. `AI_ANALYSES.original_output` is immutable. A human correction is stored only in `AI_REVIEWS.approved_output`.
7. An analysis may have at most one final review; a new analysis request creates a new analysis record.
8. An alert has at most one MVP incident. Reopening changes incident state; it does not create a duplicate.
9. Audit events are append-only through the application API.
10. Hard deletion is excluded from the MVP. Assets use archive state; operational history is retained according to the documented course limitation.

## Index baseline

| Table | Index |
|---|---|
| `users` | unique `(organization_id, lower(email))`; `(organization_id, status)` |
| `assets` | `(organization_id, status)`; `(organization_id, criticality)`; partial unique active identifier |
| `alerts` | `(organization_id, status, observed_at desc)`; `(organization_id, approved_severity)`; `(asset_id, observed_at desc)` |
| `alert_status_history` | `(alert_id, changed_at)` |
| `ai_analyses` | `(alert_id, created_at desc)`; `(organization_id, status)` |
| `incidents` | `(organization_id, status, priority)`; `(owner_id, status)` |
| `incident_actions` | `(incident_id, occurred_at)` |
| `audit_events` | `(organization_id, occurred_at desc)` and `(resource_type, resource_id, occurred_at)` |

## Migration order

1. C02: identity, roles, organization timestamps, and audit correlation support.
2. C04: assets.
3. C07: alerts, evidence, and status history.
4. C11: AI analyses and reviews.
5. C13: incidents and actions.

