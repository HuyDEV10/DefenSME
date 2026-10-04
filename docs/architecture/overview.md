# Architecture overview

Supporting implementation baselines:

- [Logical ERD](erd.md)
- [API overview](api-overview.md)
- [API error contract](error-contract.md)
- [RBAC matrix](rbac-matrix.md)

## Current decision

DefenSME starts as a modular monolith for product logic, with a small separate AI service. This keeps authentication, authorization, transactions, and audit rules in one backend while isolating model-provider code and Python AI tooling.

## Service responsibilities

### Frontend

- Present actionable security posture and alert information.
- Distinguish verified facts from AI-generated advice.
- Require explicit confirmation for state-changing actions.

### Backend

- Own users, organizations, roles, assets, alerts, incidents, and audit records.
- Validate all input and enforce tenant boundaries.
- Call the AI service through a replaceable adapter.
- Store AI request metadata, output, reviewer decision, and final corrected result.

### AI service

- Accept normalized alert context without unnecessary sensitive data.
- Return structured, schema-validated analysis.
- Support a deterministic mock provider for development and testing.
- Never connect directly to the product database.

### PostgreSQL

- Act as the system of record.
- Store tenant-scoped operational data and immutable audit events.

## Planned product modules

1. Identity and organization
2. Asset inventory
3. Alert management
4. AI analysis
5. Incident response workflow
6. Dashboard and reports
7. Audit and governance

## Security principles

- Deny by default and enforce least privilege.
- Keep secrets outside source control.
- Validate tenant ownership on every protected resource.
- Treat model output as untrusted input.
- Record human review of AI recommendations.
- Avoid autonomous remediation in the MVP.
