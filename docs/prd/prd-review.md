# Human review of PRD v0.1

| Issue | Risk | Correction in v1.0 |
|---|---|---|
| No stable IDs | Work and tests cannot be traced. | Added `FR`, `AI`, `SEC`, and `NFR` IDs. |
| “Human approval” unclear | UI and persistence become inconsistent. | Defined accept/edit/reject plus immutable original output and reviewer metadata. |
| Roles implicit | Authorization can diverge. | Defined ADMIN, IT_MANAGER, ANALYST, and VIEWER. |
| AI failure absent | Core workflow may stop. | Manual handling continues; failure is safe, visible, retryable, and audited. |
| Import validation absent | Invalid/hostile files may enter the system. | Added format, limit, field, and row-error requirements. |
| Metrics vague | Results may mislead. | Defined course-MVP indicators from stored data. |
| Security implicit | A security product would have weak requirements. | Added tenant isolation, secrets, validation, injection resistance, audit, and data minimization. |
| Market/performance claims unsupported | Assumptions may be reported as facts. | Marked evidence gaps and added a validation plan. |

Rejected scope: multiple SIEM integrations, autonomous device isolation or blocking, custom foundation-model training, production billing/multi-tenancy, malware sandboxing, and endpoint agents.

