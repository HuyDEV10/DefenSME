# Role-based access control matrix

Authorization is enforced in Spring Boot after authentication and organization ownership checks. The frontend may hide unavailable controls but is never the security authority.

Legend: `R` read, `C` create, `U` update, `A` archive/administrative action, `—` denied.

| Capability | ADMIN | IT_MANAGER | ANALYST | VIEWER |
|---|:---:|:---:|:---:|:---:|
| Organization settings | R/U | R | — | — |
| Users and roles | C/R/U/A | R | — | — |
| Audit events | R | R | — | — |
| Assets | R | C/R/U/A | C/R/U | R |
| Alerts | R | C/R/U/A | C/R/U | R |
| Import alerts | — | C | C | — |
| Assign alerts | — | A | — | — |
| Request AI analysis | — | C | C | — |
| View original unverified AI output | R | R | R | — |
| View approved guidance | R | R | R | R |
| Accept/edit/reject LOW–HIGH analysis | — | A | A | — |
| Accept/edit/reject CRITICAL analysis | — | A | Conditional* | — |
| Create incident from alert | — | C | C | — |
| Assign incident owner | — | A | — | — |
| Update owned incident/actions | — | U | U | — |
| Resolve incident | — | A | A when assigned | — |
| Close or reopen incident | — | A | — | — |
| Dashboard | R | R | R | R |
| Management report | R | R | R | R |

`*` The product owner must resolve OQ-04 before C03/C12. Safe default: only IT_MANAGER may approve CRITICAL analyses.

## Enforcement order

1. Authenticate the caller.
2. Resolve caller organization and active roles from server-side identity.
3. Load the target by `(organization_id, resource_id)`; never load by identifier and filter afterward.
4. Apply action permission and resource-specific rules such as assignment or severity.
5. Validate state transition and optimistic-lock version.
6. Perform the mutation in one transaction and append an audit event.

## Sensitive response rules

- VIEWER sees approved AI guidance, never raw unverified output or prompt metadata.
- ANALYST sees only incidents/alerts allowed by organization scope; assignment rules further restrict mutation.
- Audit `event_data` is a redacted summary, not a copy of credentials, tokens, raw files, or secret provider payloads.
- Disabled users cannot authenticate or refresh sessions.
- ADMIN manages access but does not receive operational mutation rights unless also granted IT_MANAGER/ANALYST.

