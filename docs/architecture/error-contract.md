# API error contract

All Spring Boot APIs return one safe machine-readable shape. Internal details, stack traces, SQL, secrets, and provider payloads are never returned to clients.

```json
{
  "type": "https://defensme.local/problems/validation-failed",
  "title": "Validation failed",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "detail": "One or more fields are invalid.",
  "instance": "/api/v1/alerts",
  "correlationId": "a8db6bd4-8f86-45a6-85a7-83316503caf5",
  "timestamp": "2026-10-04T07:00:00Z",
  "errors": [
    { "field": "title", "code": "SIZE", "message": "Title must contain 3 to 200 characters." }
  ]
}
```

## Rules

- `code` is stable for client behavior; `detail` is safe human-readable text.
- `correlationId` links the response to structured logs and audit data without exposing internals.
- `errors` is present only for field/row validation and is otherwise omitted.
- Cross-organization identifiers normally return `404 RESOURCE_NOT_FOUND` to avoid resource disclosure.
- Authentication failures do not reveal whether an email exists.
- AI provider failures map to DefenSME codes; raw provider messages remain in restricted server logs only when safe.

## Code registry

| HTTP | Code | Meaning |
|---:|---|---|
| 400 | `VALIDATION_FAILED` | Request fields failed validation. |
| 400 | `IMPORT_FORMAT_INVALID` | File encoding, schema, or type is invalid. |
| 400 | `IMPORT_LIMIT_EXCEEDED` | File size or row limit is exceeded. |
| 401 | `AUTHENTICATION_REQUIRED` | Valid authentication is missing. |
| 401 | `INVALID_CREDENTIALS` | Login failed without account disclosure. |
| 403 | `ACCESS_DENIED` | Authenticated user lacks an allowed action. |
| 404 | `RESOURCE_NOT_FOUND` | Resource is absent or outside the user's organization. |
| 409 | `STATE_TRANSITION_NOT_ALLOWED` | Requested lifecycle transition is invalid. |
| 409 | `RESOURCE_VERSION_CONFLICT` | Optimistic-lock version is stale. |
| 409 | `ANALYSIS_ALREADY_REVIEWED` | Analysis already has a final human review. |
| 409 | `RESOURCE_ALREADY_EXISTS` | A scoped uniqueness invariant was violated. |
| 422 | `AI_OUTPUT_INVALID` | AI output failed the versioned response schema. |
| 503 | `AI_SERVICE_UNAVAILABLE` | Analysis is temporarily unavailable; manual workflow continues. |
| 504 | `AI_SERVICE_TIMEOUT` | Configured AI timeout elapsed. |
| 500 | `INTERNAL_ERROR` | Unexpected server failure with a correlation identifier. |

## Import row error

Import results use the same field-error vocabulary without failing the whole valid subset:

```json
{
  "importId": "22096060-42cb-431a-a1f8-a76a2036eafe",
  "accepted": 8,
  "rejected": 2,
  "rows": [
    {
      "row": 4,
      "status": "REJECTED",
      "errors": [
        { "field": "observedSeverity", "code": "ENUM", "message": "Use LOW, MEDIUM, HIGH, or CRITICAL." }
      ]
    }
  ]
}
```

