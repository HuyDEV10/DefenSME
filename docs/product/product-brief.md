# DefenSME product brief

**Status:** Approved baseline for PRD v1.0  
**Date:** 2026-10-04

## Product statement

DefenSME is an AI-assisted cybersecurity alert triage and incident-guidance platform for Vietnamese SMEs. It helps IT staff understand alerts, prioritize response, follow remediation guidance, and report security posture without requiring a dedicated SOC.

## Problem and users

SMEs often receive technical signals without a consistent way to understand business impact, decide priority, assign ownership, and preserve response history.

- **Primary — IT Manager / IT staff:** registers assets, reviews alerts, verifies AI advice, and handles incidents.
- **Secondary — Business owner / manager:** consumes understandable posture and incident reports.
- **Administrator:** manages users, roles, and auditable access.

## MVP promise

An authorized user can register an asset, create or import an alert, obtain structured Vietnamese guidance, explicitly review it, track response work, close the incident, and see the result in an auditable dashboard.

## Principles and constraints

- Human decisions override AI suggestions; original and corrected outputs remain traceable.
- Business rules and authorization live in Spring Boot; FastAPI is advisory and database-independent.
- Sensitive data is minimized before model processing.
- Initial alert sources are manual entry and controlled JSON/CSV demo imports.
- No autonomous destructive remediation.
- Existing stack: React/TypeScript, Spring Boot, FastAPI, PostgreSQL, Docker Compose.

## Assumptions requiring validation

| ID | Assumption | Validation |
|---|---|---|
| AS-01 | Manual/file intake is sufficient to validate the first workflow. | Observe target users performing a task walkthrough. |
| AS-02 | Vietnamese explanations improve comprehension. | Compare comprehension with the raw alert. |
| AS-03 | Visible evidence and review controls prevent overtrust. | Prototype-test accept/edit/reject behavior. |
| AS-04 | Curated or synthetic alerts can support course evaluation. | Expert-review representative scenarios. |

## Open questions

| ID | Question | Resolve before |
|---|---|---|
| OQ-01 | Which model provider follows the deterministic mock? | AI integration sprint |
| OQ-02 | Which alert fields may leave the backend for model processing? | AI integration sprint |
| OQ-03 | Which public or synthetic dataset supports evaluation? | Test-data preparation |
| OQ-04 | Who may approve CRITICAL analyses? | Authorization implementation |
| OQ-05 | What deadline and team capacity determine sprint estimates? | Backlog estimation |
| OQ-06 | Which interviews or lecturer feedback count as validation? | Final PRD review |

