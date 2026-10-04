# AI evidence standard

Every planned commit `C01`–`C24` must include a reviewable AI evidence record in `docs/ai-evidence/commits/Cxx.md`. A commit cannot be marked `Done` when this record is missing, vague, or inconsistent with the implementation.

## Purpose

The evidence must demonstrate that AI was used as an assistive tool and that a human remained responsible for requirements, architecture, code, security, testing, and final acceptance. It must enable a lecturer or teammate to reconstruct what was asked, what AI proposed, how the proposal was checked, and why the final decision was accepted or rejected.

## Required record

Each commit record contains all sections below.

### 1. Identification

- Planned ID, date, final commit message, author/reviewer, AI tool or model when known.
- Related PRD IDs, architecture documents, source files, and tests.

### 2. Objective and research question

- State one concrete objective.
- State the question the AI is helping answer.
- Define the acceptance checkpoint before showing output.

### 3. Authoritative context

- List exact repository documents/files supplied as ground truth.
- Separate confirmed facts, assumptions, and open questions.
- Do not include secrets, credentials, personal data, or confidential production logs.

### 4. Exact prompt

- Preserve the prompt verbatim in a fenced block.
- Include role, objective, context, task, constraints, required output, and verification expectations.
- If several prompts materially changed the result, preserve each version in chronological order.
- Never replace the actual prompt with a one-line summary.

### 5. AI output

- Preserve a concise but faithful output summary and link to generated artifacts/diffs.
- Record suggestions that were accepted, edited, or rejected.
- Do not paste huge generated files when repository links or diffs are more auditable.

### 6. Human evaluation

Evaluate the output against explicit criteria:

| Criterion | Evidence | Result |
|---|---|---|
| Requirement correctness | PRD/acceptance criteria comparison | Pass/Fail/Partial |
| Technical correctness | Build, tests, migration, lint, typecheck | Pass/Fail/Not run |
| Security and privacy | RBAC, tenant, secrets, untrusted input review | Pass/Fail/Not applicable |
| Consistency | Architecture/API/database/code comparison | Pass/Fail/Partial |
| Scope control | Planned commit and non-goals comparison | Pass/Fail |

### 7. Verification evidence

- Record exact commands/checks and summarized results.
- Record failures and the correction made; do not show only the final successful attempt.
- Explain any check that could not run and the remaining risk.

### 8. Human decision

- Record `Accepted`, `Accepted with edits`, or `Rejected` for each material AI proposal.
- Name the final human-controlled change.
- Record deviations, limitations, and the next action.

## Scientific prompt quality checklist

A prompt is acceptable only when it is:

- **Specific:** one bounded problem and defined deliverables.
- **Grounded:** authoritative files and facts are named.
- **Reproducible:** another reviewer can provide the same context and understand the requested procedure.
- **Falsifiable:** acceptance and failure criteria can show the answer is wrong.
- **Constrained:** exclusions, safety boundaries, and non-goals are explicit.
- **Structured:** expected output format and traceability are defined.
- **Verifiable:** tests, comparisons, or review methods are required.

## Prohibited evidence patterns

- “Asked AI to write code” without the actual prompt.
- Claiming an AI suggestion is correct only because it compiled.
- Omitting failed prompts, rejected output, or manual corrections.
- Inventing tool/model names, test results, customer evidence, or performance metrics.
- Storing secrets, tokens, passwords, raw personal data, or unsafe operational commands.
- Marking a commit `Done` before evidence and verification are committed.

