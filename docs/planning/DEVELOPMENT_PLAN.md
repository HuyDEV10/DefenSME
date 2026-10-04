# DefenSME MVP development plan

**Baseline date:** 2026-10-04  
**Target release:** 2026-11-30  
**Cadence:** three planned commits per development week  
**Scope of “100%”:** all Must requirements and approved release criteria in PRD v1.0

This document is the canonical implementation order. Planned dates are targets, not gates: a commit may be completed earlier or later, but its actual date must be recorded in `PROGRESS.md`. Do not split work artificially to satisfy a commit count.

## Mandatory AI evidence gate

Every C01–C24 implementation commit must add or update `docs/ai-evidence/commits/Cxx.md` using the scientific structure in `EVIDENCE_STANDARD.md` and `PROMPT_TEMPLATE.md`. The record must preserve the exact prompt, grounded inputs, constraints, output summary, failed attempts, verification results, corrections/rejections, and final human decision. Missing evidence blocks the commit from `Done` even when code builds.

## Schedule

| ID | Target | Commit message | Required work | Acceptance checkpoint |
|---|---|---|---|---|
| C01 | 05/10 | `docs: add technical design, ERD and implementation backlog` | ERD, API overview, RBAC matrix, backlog, error contract, traceability update | Technical design is consistent with PRD v1.0 and current service boundaries. |
| C02 | 07/10 | `feat(backend): add identity, organization and core domain schema` | User/role migrations, organization scope, base domain conventions, default roles | Empty database migrates successfully and core schema tests pass. |
| C03 | 10/10 | `feat(auth): implement authentication and role authorization` | Login, adaptive password hashing, token/session, Spring Security, authorization tests | Valid login succeeds; invalid login and unauthorized access fail safely. |
| C04 | 12/10 | `feat(asset): implement asset domain and REST API` | Asset schema, CRUD, search, pagination, organization isolation | Asset API and tenant-boundary integration tests pass. |
| C05 | 14/10 | `feat(frontend): add asset management screens` | Asset list, create/edit/detail, search/filter/pagination, validation states | Asset workflow works from UI through persistence. |
| C06 | 17/10 | `test(asset): add asset integration tests and demo data` | Unit/integration/authorization tests, demo seed, API documentation | Asset module checkpoint and documentation are complete. |
| C07 | 19/10 | `feat(alert): implement alert domain and lifecycle` | Alert/evidence/history schema, CRUD, state machine, asset link, audit | Allowed transitions work; invalid transitions cannot change state. |
| C08 | 21/10 | `feat(alert): add CSV and JSON alert import` | Templates, size/row limits, field validation, row result report, demo data | Valid rows import; invalid rows are rejected with actionable errors. |
| C09 | 24/10 | `feat(frontend): add alert management and import workflow` | Alert list/form/detail, import, severity/status filters, history timeline | Manual and file alert workflows work end to end. |
| C10 | 26/10 | `feat(ai): extend structured alert analysis contract` | Business impact, limitations, schema version, confidence, mock and contract tests | Valid structured output passes; malformed output fails validation. |
| C11 | 28/10 | `feat(backend): integrate AI analysis and review persistence` | AI client, metadata/original output storage, timeout/retry, safe failure | Analysis is traceable and core workflows remain usable during AI outage. |
| C12 | 31/10 | `feat(frontend): add AI analysis and human review workflow` | Unverified label, accept/edit/reject, rejection reason, severity separation, failure UI | Alert → AI analysis → human decision is complete and auditable. |
| C13 | 02/11 | `feat(incident): implement incident domain and state transitions` | Incident/action schema, alert association, owner, state machine, validation | Incident API enforces ownership and allowed transitions. |
| C14 | 04/11 | `feat(frontend): add incident response workspace` | Incident list/detail, assignment, checklist, evidence/actions, timeline | An owner can process an assigned incident through the UI. |
| C15 | 07/11 | `test(incident): verify lifecycle, audit and authorization` | Lifecycle, close/reopen permissions, audit, no-AI-autonomy tests, demo scenario | A reviewed alert can be handled through `CLOSED` with complete history. |
| C16 | 09/11 | `feat(dashboard): add security metrics and summary APIs` | Asset/alert counts, critical unresolved, risky assets, mean resolution time, trend | Metrics use only organization-scoped persisted records. |
| C17 | 11/11 | `feat(frontend): build security dashboard` | KPI cards, charts, priority list, period filter, loading/empty/error and accessibility | Dashboard is readable, keyboard-usable, and does not rely on color alone. |
| C18 | 14/11 | `feat(report): add reviewed security reporting` | Period report, reviewed-data rules, AI narrative labels, tests | Reports exclude rejected AI output and clearly label generated narrative. |
| C19 | 16/11 | `security: harden tenant isolation and input validation` | Ownership checks, deny-by-default, file/input limits, secret review, prompt-injection boundary | Security tests cover cross-organization and malicious-input cases. |
| C20 | 18/11 | `refactor: standardize errors, logging and audit events` | Global errors, correlation ID, safe logging, audit event normalization | Errors are consistent and logs/audits contain no secrets. |
| C21 | 21/11 | `test: improve coverage, accessibility and performance checks` | Missing unit/integration/component tests, accessibility, local p95, coverage report | Quality gates pass and no critical gap remains unexplained. |
| C22 | 23/11 | `test(e2e): add complete alert-to-closure scenarios` | Login → asset → alert/import → AI review → incident → closure → dashboard/audit | Primary E2E scenarios pass on a clean environment. |
| C23 | 25/11 | `docs: complete setup, API, testing and AI evidence` | README, Docker, API, test plan/report, user guide, architecture and AI evidence | A new reviewer can install, run, test, and understand the project. |
| C24 | 28/11 | `release: prepare DefenSME v1.0.0 release candidate` | Final fixes, Compose/CI, demo seed/account, demo script, version, release checklist | `v1.0.0-rc1` is demo-ready with green CI and no blocker/critical issue. |

## Final acceptance window — 29–30/11

- Run the complete suite and a clean-environment demo.
- Verify documentation links, demo data, credentials instructions, secrets, and CI.
- Fix only genuine release blockers; do not create artificial commits.
- Complete the Definition of Done and publish tag `v1.0.0`.

## Weekly checkpoints

| Week | Result required |
|---|---|
| 1 | Technical foundation, authentication, and authorization work. |
| 2 | Asset management works end to end. |
| 3 | Alert creation/import and lifecycle work end to end. |
| 4 | Structured AI analysis and accountable human review work end to end. |
| 5 | Incident response reaches `CLOSED` with audit history. |
| 6 | Dashboard and reviewed reports are usable. |
| 7 | Security and quality gates pass for a release candidate. |
| 8 | E2E, course evidence, documentation, and release candidate are complete. |
