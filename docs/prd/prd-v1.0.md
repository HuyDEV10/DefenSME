# DefenSME Product Requirements Document v1.0

**Status:** Approved baseline for implementation planning  
**Date:** 2026-10-04  
**Scope:** Course MVP

## 1. Summary and vision

DefenSME helps Vietnamese SMEs convert technical alerts into reviewed and traceable incident-response work. It centralizes assets and alerts, produces advisory Vietnamese AI analysis, requires human review, tracks ownership/status, and presents operational summaries. The product assists a small IT team without pretending that AI replaces a security professional.

## 2. Goals

| ID | Goal | MVP indicator |
|---|---|---|
| GOAL-01 | Complete an alert-response workflow. | A seeded alert moves from NEW to CLOSED with complete history. |
| GOAL-02 | Make alerts understandable. | At least 80% of evaluation cases contain required structured fields and are rated understandable by reviewers. |
| GOAL-03 | Preserve accountable AI use. | 100% of analyses retain original output and a reviewer decision before approval. |
| GOAL-04 | Remain usable without AI. | Users can create, assign, update, and close alerts during AI outage. |

These are course acceptance targets, not market-performance claims.

## 3. Non-goals

Antivirus, endpoint detection agents, malware sandboxing, automatic scanning, autonomous isolation/blocking/deletion, enterprise SIEM replacement, custom foundation-model training, and production billing are outside the MVP.

## 4. Roles

| Role | Permissions |
|---|---|
| ADMIN | Manage organization users, roles, configuration, and audit access. |
| IT_MANAGER | Manage assets/alerts, review AI, assign/close incidents, view reports. |
| ANALYST | Create/investigate alerts, request AI, edit guidance, update assigned incidents. |
| VIEWER | Read dashboards, approved guidance, incidents, and reports. |

## 5. Primary journey

Register asset → create/import alert → validate/store as `NEW` → request structured AI analysis → human accepts/edits/rejects → triage and assign → record actions/evidence → resolve/close → report from reviewed operational data.

## 6. Functional requirements

| ID | Priority | Requirement |
|---|---|---|
| FR-AUTH-001 | Must | Authenticate users without exposing credentials. |
| FR-AUTH-002 | Must | Enforce organization-scoped role permissions in the backend. |
| FR-ASSET-001 | Must | IT_MANAGER and ANALYST can create, view, update, search, and archive assets. |
| FR-ALERT-001 | Must | Authorized users can create alerts with source, observed severity, time, evidence, and optional asset. |
| FR-ALERT-002 | Must | Import documented UTF-8 JSON/CSV and return row-level errors; invalid rows are never stored. |
| FR-ALERT-003 | Must | Enforce `NEW → TRIAGED → IN_PROGRESS → RESOLVED → CLOSED`; IT_MANAGER may reopen. |
| FR-ALERT-004 | Must | Record assignment, comments, evidence references, remediation actions, and status history. |
| FR-AI-001 | Must | Request one analysis using minimized normalized context for a stored alert. |
| FR-AI-002 | Must | Show original AI output separately and label it unverified. |
| FR-AI-003 | Must | Authorized reviewers accept, edit, or reject; rejection requires a reason. |
| FR-INC-001 | Must | Associate an incident with a triaged alert and assign an owner. |
| FR-DASH-001 | Should | Show asset count, alerts by severity/state, unresolved critical alerts, risky assets, and mean resolution time. |
| FR-REPORT-001 | Should | Produce a period summary from reviewed data and label AI narrative. |
| FR-AUDIT-001 | Must | Record actor, organization, action, resource, time, and safe before/after metadata for protected changes. |

## 7. AI requirements

| ID | Requirement |
|---|---|
| AI-001 | Send only necessary normalized context; exclude secrets and unnecessary personal data. |
| AI-002 | Validate a versioned schema: Vietnamese summary, severity, confidence 0–1, business impact, remediation steps, limitations, and `requires_human_review=true`. |
| AI-003 | Reject malformed output without silently inferring missing fields. |
| AI-004 | Preserve provider/model metadata, prompt version, timestamp, original output, reviewer, decision, correction, and reason. |
| AI-005 | On timeout/unavailability, keep manual workflow available and show a retryable safe failure. |
| AI-006 | Never execute AI-generated commands or destructive actions. |
| AI-007 | Evaluate with curated/synthetic cases and record reviewer disagreement. |

## 8. Security and privacy

| ID | Requirement |
|---|---|
| SEC-001 | Use an approved adaptive password hash; never log or return passwords. |
| SEC-002 | Enforce deny-by-default authorization and organization ownership in the backend. |
| SEC-003 | Keep secrets outside source control; `.env.example` contains placeholders only. |
| SEC-004 | Validate lengths, types, file formats, row counts, and allowed values at trust boundaries. |
| SEC-005 | Treat evidence/imported text as untrusted and delimit it from model instructions. |
| SEC-006 | Audit authentication, role, alert-state, AI-request, and review events without secrets. |
| SEC-007 | Document retention/deletion limitations before any production use. |

## 9. Non-functional requirements

| ID | Requirement |
|---|---|
| NFR-001 | Non-AI APIs target p95 under 2 seconds for the documented local course dataset. |
| NFR-002 | AI requests have a configurable timeout, initially 30 seconds. |
| NFR-003 | APIs use consistent machine-readable errors and safe user messages. |
| NFR-004 | Versioned migrations reproduce an empty PostgreSQL database. |
| NFR-005 | CI runs backend tests, AI tests, frontend lint, and production build. |
| NFR-006 | Core flows are keyboard operable and do not rely only on color. |

## 10. Data and architecture

Core entities: Organization, User, Role, Asset, Alert, AlertEvidence, AIAnalysis, AIReview, Incident, IncidentAction, AuditEvent, and ReportSnapshot. Operational entities are organization-scoped. Spring Boot owns state, validation, authorization, and audit; React presents workflows; FastAPI provides provider-neutral structured analysis without database access; PostgreSQL is the system of record.

## 11. Risks

| Risk | Mitigation |
|---|---|
| Hallucinated or unsafe guidance | Structured output, human review, visible uncertainty, curated evaluation, no execution. |
| Sensitive provider data | Minimization, redaction policy, configurable provider, restricted logging. |
| Severity overtrust | Preserve observed, recommended, and approved severity separately. |
| Scope growth | Protect Must/Should priorities; move integrations to roadmap. |
| Unrealistic demo data | Expert-review synthetic scenarios and document limitations. |

## 12. Roadmap and change control

- **MVP:** assets, manual/file alerts, structured AI analysis, human review, incidents, audit, dashboard.
- **Next:** one read-only external integration, stronger evaluation, notifications, exportable reports.
- **Future:** production tenant hardening, commercial operations, selected approved automation, broader integrations.

Assumptions and open questions are in `../product/product-brief.md`. A documented PRD revision is required to change an MVP requirement.

