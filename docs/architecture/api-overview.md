# DefenSME API overview

**Public base path:** `/api/v1`  
**Internal AI path:** `${AI_SERVICE_URL}/api/v1`  
**Payload format:** `application/json; charset=utf-8`

Spring Boot is the public API and authorization authority. The browser must not call FastAPI directly. FastAPI accepts minimized normalized context from Spring Boot and never accesses PostgreSQL.

## Conventions

- Resource identifiers are UUID strings.
- Timestamps are ISO 8601 UTC values.
- Collection defaults: `page=0`, `size=20`, maximum `size=100`, stable secondary sort by `id`.
- Mutating requests validate organization ownership in the backend.
- `POST` returns `201`; synchronous actions return `200`; deletion-like archive operations return `204` when no body is needed.
- Imports use `multipart/form-data`; accepted templates are UTF-8 CSV and JSON only.
- Error bodies follow [`error-contract.md`](error-contract.md).
- AI results are unverified until an authorized review decision exists.

## Authentication and users

| Method | Path | Purpose | Roles |
|---|---|---|---|
| POST | `/auth/login` | Authenticate and issue access credentials. | Public |
| POST | `/auth/refresh` | Rotate an eligible access credential. | Authenticated |
| POST | `/auth/logout` | Revoke/end the current session. | Authenticated |
| GET | `/me` | Return current user, organization, and roles. | Authenticated |
| GET | `/users` | List organization users. | ADMIN |
| POST | `/users` | Create an organization user. | ADMIN |
| PATCH | `/users/{userId}/roles` | Replace approved application roles. | ADMIN |
| PATCH | `/users/{userId}/status` | Activate or disable a user. | ADMIN |

## Assets

| Method | Path | Purpose | Roles |
|---|---|---|---|
| GET | `/assets` | Search/filter/paginate active or archived assets. | ADMIN, IT_MANAGER, ANALYST, VIEWER |
| POST | `/assets` | Create an asset. | IT_MANAGER, ANALYST |
| GET | `/assets/{assetId}` | Read an asset. | ADMIN, IT_MANAGER, ANALYST, VIEWER |
| PUT | `/assets/{assetId}` | Update mutable asset fields. | IT_MANAGER, ANALYST |
| POST | `/assets/{assetId}/archive` | Archive an asset without deleting history. | IT_MANAGER |

## Alerts

| Method | Path | Purpose | Roles |
|---|---|---|---|
| GET | `/alerts` | Filter by status, severity, asset, assignee, and observed interval. | All roles |
| POST | `/alerts` | Create an alert in `NEW`. | IT_MANAGER, ANALYST |
| POST | `/alerts/imports` | Import controlled CSV/JSON and return row results. | IT_MANAGER, ANALYST |
| GET | `/alerts/{alertId}` | Read alert, evidence, review state, and history. | All roles |
| PUT | `/alerts/{alertId}` | Update editable alert details. | IT_MANAGER, ANALYST |
| POST | `/alerts/{alertId}/transitions` | Request an allowed lifecycle transition with reason. | IT_MANAGER, ANALYST; close/reopen restricted by RBAC |
| POST | `/alerts/{alertId}/assignments` | Assign or clear an analyst. | IT_MANAGER |
| POST | `/alerts/{alertId}/evidence` | Add bounded textual/reference evidence. | IT_MANAGER, ANALYST |

## AI analysis and review

| Method | Path | Purpose | Roles |
|---|---|---|---|
| POST | `/alerts/{alertId}/ai-analyses` | Request structured analysis; return or create a traceable analysis. | IT_MANAGER, ANALYST |
| GET | `/alerts/{alertId}/ai-analyses` | List analysis attempts and review status. | ADMIN, IT_MANAGER, ANALYST |
| GET | `/ai-analyses/{analysisId}` | Read original output and approved guidance according to role. | ADMIN, IT_MANAGER, ANALYST, VIEWER* |
| POST | `/ai-analyses/{analysisId}/reviews` | Accept, edit, or reject one analysis. | IT_MANAGER, ANALYST according to critical-review rule |

`VIEWER` receives approved guidance only; original unverified content is not exposed in the normal viewer response.

Internal call:

| Method | FastAPI path | Contract |
|---|---|---|
| POST | `/api/v1/analyses/alerts` | Normalized alert context → versioned structured analysis |
| GET | `/health` | Provider-neutral liveness metadata |

## Incidents

| Method | Path | Purpose | Roles |
|---|---|---|---|
| GET | `/incidents` | Filter by status, priority, owner, and interval. | All roles |
| POST | `/alerts/{alertId}/incident` | Create the alert's incident after triage. | IT_MANAGER, ANALYST |
| GET | `/incidents/{incidentId}` | Read incident, actions, and timeline. | All roles |
| POST | `/incidents/{incidentId}/assignments` | Assign an owner. | IT_MANAGER |
| POST | `/incidents/{incidentId}/transitions` | Apply an allowed transition with reason. | IT_MANAGER, assigned ANALYST |
| POST | `/incidents/{incidentId}/actions` | Record a remediation or evidence action. | IT_MANAGER, assigned ANALYST |

## Dashboard, reports, and audit

| Method | Path | Purpose | Roles |
|---|---|---|---|
| GET | `/dashboard/security-summary` | Organization-scoped metrics for a period. | All roles |
| GET | `/reports/security-summary` | Reviewed-data report for a period. | ADMIN, IT_MANAGER, VIEWER |
| GET | `/audit-events` | Search auditable events; metadata is redacted by policy. | ADMIN, IT_MANAGER |

## State transitions

Alert transitions:

```text
NEW -> TRIAGED -> IN_PROGRESS -> RESOLVED -> CLOSED
RESOLVED -> IN_PROGRESS
CLOSED -> IN_PROGRESS (IT_MANAGER only, reason required)
```

Incident transitions use `OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED`, with reopen to `IN_PROGRESS` by IT_MANAGER. Invalid transitions return `409 STATE_TRANSITION_NOT_ALLOWED` without mutation.

## Idempotency and concurrency

- Lifecycle transitions use optimistic locking/version checks; stale writes return `409 RESOURCE_VERSION_CONFLICT`.
- Import requests return an import identifier. Repeating the same file is allowed but duplicate-detection results must be explicit.
- AI analysis retries create a new attempt; they never overwrite an earlier original output.
- A duplicate final review for the same analysis returns `409 ANALYSIS_ALREADY_REVIEWED`.

