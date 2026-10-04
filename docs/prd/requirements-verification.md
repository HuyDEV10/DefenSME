# User stories, acceptance criteria, and traceability

## User stories

| ID | Requirement | Story |
|---|---|---|
| US-01 | FR-ASSET-001 | As IT staff, I want to register and find assets so alerts map to owned systems. |
| US-02 | FR-ALERT-001 | As an Analyst, I want to record an alert so it enters a consistent workflow. |
| US-03 | FR-ALERT-002 | As an Analyst, I want row-level import results so I can correct data safely. |
| US-04 | FR-AI-001/002 | As an Analyst, I want an unverified Vietnamese explanation so I can assess the alert. |
| US-05 | FR-AI-003 | As a reviewer, I want to accept, edit, or reject AI guidance so the final decision is accountable. |
| US-06 | FR-ALERT-003/004 | As an owner, I want to track state, evidence, and actions so response is auditable. |
| US-07 | FR-DASH-001 | As an IT Manager, I want unresolved-risk metrics so I can prioritize capacity. |
| US-08 | FR-REPORT-001 | As an owner, I want an understandable period summary so I can discuss posture with IT. |

## Acceptance criteria

### AC-01 — Create alert

**Given** an authenticated IT_MANAGER or ANALYST, **when** valid alert data is submitted, **then** one organization-scoped alert is stored as `NEW`, normalized data is returned, and an audit event is written. Invalid fields create no alert and return structured errors.

### AC-02 — Import alerts

**Given** an authorized user and documented UTF-8 CSV/JSON, **when** a permitted file is imported, **then** each valid row is accepted, each invalid row has a field-level reason, invalid rows are not stored, and file limits are enforced.

### AC-03 — Analyze alert

**Given** a visible stored alert, **when** analysis is requested, **then** the backend sends minimized context, accepts only schema-valid output, stores it unverified, and displays original output, metadata, confidence, and a review warning.

### AC-04 — Review analysis

**Given** unverified analysis and an authorized reviewer, **when** it is accepted, edited, or rejected, **then** the original remains immutable and the decision, reviewer, time, correction, and reason are stored. Rejection requires a reason.

### AC-05 — AI unavailable

**Given** timeout or invalid output, **when** analysis is requested, **then** no partial output is approved, failure is safely recorded, retry is offered, and manual incident work continues.

### AC-06 — Incident lifecycle

**Given** a triaged alert, **when** an authorized user requests a transition, **then** the backend enforces the state machine and records actor, time, old/new state, and notes. Invalid transitions make no change.

### AC-07 — Organization isolation

**Given** a user in organization A, **when** an identifier owned by organization B is requested, **then** B's data is neither returned nor modified and the attempt is handled by security policy.

### AC-08 — Dashboard

**Given** stored organization data, **when** the dashboard opens, **then** metrics use only that organization's records and critical unresolved alerts are distinguishable without color alone.

## Traceability

| Goal | Requirement | Story | Criteria | Planned verification |
|---|---|---|---|---|
| GOAL-01 | FR-ASSET-001, FR-AUTH-002 | US-01 | AC-07 | API integration and authorization tests |
| GOAL-01 | FR-ALERT-001/002 | US-02/03 | AC-01/02/07 | Validation, persistence, tenant, import-limit tests |
| GOAL-01 | FR-ALERT-003/004, FR-INC-001 | US-06 | AC-06 | State-machine integration tests |
| GOAL-02 | FR-AI-001/002 | US-04 | AC-03/05 | Contract, schema, timeout, usability review |
| GOAL-03 | FR-AI-003, FR-AUDIT-001 | US-05 | AC-04 | Immutability and audit integration tests |
| GOAL-04 | AI-005 | US-04/06 | AC-05 | AI outage test |
| GOAL-01 | FR-DASH-001 | US-07 | AC-08 | Query and component tests |
| GOAL-02 | FR-REPORT-001 | US-08 | AC-04 | Data-source and AI-labeling tests |

