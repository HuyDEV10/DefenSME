# Scientific prompt and evidence template

Copy this file to `docs/ai-evidence/commits/Cxx.md` and replace every bracketed field. Keep the exact prompts and remove instructional placeholders before committing.

## 1. Identification

- **Planned ID:** `[Cxx]`
- **Date:** `[YYYY-MM-DD]`
- **Commit message:** `[type(scope): description]`
- **AI tool/model:** `[Known name/version or “not exposed by tool”]`
- **Human reviewer:** `[Name or team role]`
- **Related requirements:** `[FR-..., AI-..., SEC-..., NFR-...]`
- **Authoritative files:** `[paths]`

## 2. Objective

**Objective:** `[One measurable outcome.]`

**Research/engineering question:** `[What question should the AI help answer?]`

**Acceptance checkpoint:** `[Observable conditions required for completion.]`

## 3. Context classification

### Confirmed facts

- `[Fact supported by repository source.]`

### Assumptions

- `[ASSUMPTION] ...`

### Open questions

- `[OPEN QUESTION] ...`

## 4. Exact prompt v1

```text
ROLE
You are [bounded technical role].

OBJECTIVE
[Concrete objective.]

AUTHORITATIVE CONTEXT
- [file/path: relevant fact]

TASK
1. [Required operation]
2. [Required operation]

CONSTRAINTS
- [Scope and safety rule]
- Do not invent missing facts; mark them as assumptions or open questions.

REQUIRED OUTPUT
1. [Artifact/diff/schema]
2. [Traceability or rationale]

VERIFICATION
- [Test/build/lint/migration/security comparison]
- Report failures and remaining risk explicitly.
```

## 5. Prompt iterations

Record v2/v3 only when feedback materially changed the output. Explain why each revision was needed.

## 6. AI output summary

- **Proposed:** `[material proposals]`
- **Artifacts/diff:** `[repository paths or diff]`
- **Uncertainty stated by AI:** `[limitations]`

## 7. Human evaluation

| Criterion | Evidence | Result | Human notes |
|---|---|---|---|
| Requirement correctness | `[source/check]` | `[Pass/Fail/Partial]` | `[notes]` |
| Technical correctness | `[commands/results]` | `[Pass/Fail/Not run]` | `[notes]` |
| Security and privacy | `[review/test]` | `[Pass/Fail/N/A]` | `[notes]` |
| Consistency | `[comparison]` | `[Pass/Fail/Partial]` | `[notes]` |
| Scope control | `[plan comparison]` | `[Pass/Fail]` | `[notes]` |

## 8. Verification log

| Check/command | Expected | Actual | Result |
|---|---|---|---|
| `[command or review]` | `[expected]` | `[actual]` | `[Pass/Fail]` |

## 9. Corrections and rejected suggestions

- **Corrected:** `[AI output changed by human and why]`
- **Rejected:** `[proposal rejected and why]`
- **Failed attempt:** `[failure, diagnosis, correction]`

## 10. Final human decision

- **Decision:** `[Accepted / Accepted with edits / Rejected]`
- **Final responsibility:** `[human-controlled result]`
- **Remaining risks:** `[known limitations]`
- **Next action:** `[next planned commit or validation]`

