# Plan execution protocol

Use this protocol whenever the user asks to continue the DefenSME schedule.

## Recognized instructions

The following requests all mean “execute the next eligible planned commit”:

- “Làm commit tiếp theo.”
- “Tiếp tục theo kế hoạch.”
- “Thực hiện lịch trình DefenSME.”
- “Làm Cxx.”

If a specific ID is named, execute that ID only when its prerequisites are complete. Otherwise explain the dependency and continue with the next eligible item unless the user explicitly changes the order.

## Before implementation

1. Read `PROGRESS.md`, `DEVELOPMENT_PLAN.md`, PRD v1.0, and relevant architecture/code.
2. Fetch the current `main` state and preserve unrelated changes.
3. Select the first `Planned` item whose dependencies are complete.
4. Treat its target date as guidance. Never skip it only because the date has passed or not arrived.
5. Restate the selected ID, scope, acceptance checkpoint, and expected files before editing.

## During implementation

- Keep the change atomic and within the selected planned item.
- Add or update migrations, tests, API documentation, and AI evidence when applicable.
- Preserve human-in-the-loop and no-autonomous-remediation boundaries.
- Do not add unrelated roadmap features merely because they are convenient.
- If the plan is technically invalid, stop before expanding scope and record the proposed change.

## Before committing

- Run every relevant test, lint, build, migration, and security check available.
- Compare the result with the commit acceptance checkpoint.
- Update `PROGRESS.md` with status, actual date, evidence, deviations, and next ID.
- Update the PRD/architecture/traceability only when behavior or decisions changed.
- Use the planned commit message unless the actual scope requires a more accurate conventional message.

## Completion report

Report:

1. Planned ID and final commit message.
2. What changed and why.
3. Files changed.
4. Tests/checks and results.
5. GitHub commit link.
6. Deviations or remaining risks.
7. The next planned ID.

## Date and sequence policy

- Target dates may move; actual dates are authoritative.
- Sequence may change only for a documented dependency, blocker, or explicit user decision.
- A missed week does not justify combining unsafe scopes into one commit.
- More than three commits in a week is allowed for real fixes or review corrections.
- Fewer than three is allowed only when blocked or when a planned unit legitimately spans the boundary; record the reason.
- Never generate empty or cosmetic commits to satisfy cadence.

