# DefenSME

DefenSME is an AI-assisted cybersecurity defense platform designed for small and medium-sized enterprises. The MVP focuses on receiving security alerts, helping users understand and prioritize them, recommending remediation steps, and tracking incident resolution.

## MVP scope

- Security asset inventory
- Alert ingestion and lifecycle management
- AI-assisted alert explanation, prioritization, and remediation guidance
- Human review before any operational action
- Incident workflow and audit history
- Security dashboard and management reporting

The initial codebase contains service boundaries and health checks. The reviewed course-MVP requirements are approved as an implementation-planning baseline in [`docs/prd/prd-v1.0.md`](docs/prd/prd-v1.0.md).

## Architecture

| Component | Technology | Responsibility |
|---|---|---|
| Frontend | React, TypeScript, Vite | SME security dashboard |
| Backend | Java 21, Spring Boot | Business rules, API, persistence, authorization |
| AI service | Python 3.12, FastAPI | AI analysis behind a provider-neutral boundary |
| Database | PostgreSQL | Product and audit data |

See [docs/architecture/overview.md](docs/architecture/overview.md) for design decisions.

Implementation contracts are defined in the [ERD](docs/architecture/erd.md), [API overview](docs/architecture/api-overview.md), [error contract](docs/architecture/error-contract.md), and [RBAC matrix](docs/architecture/rbac-matrix.md).

## Quick start with Docker

1. Copy `.env.example` to `.env`.
2. Replace `POSTGRES_PASSWORD` with a local development password.
3. Run:

```bash
docker compose up --build
```

Services:

- Frontend: http://localhost:5173
- Backend health: http://localhost:8080/api/v1/health
- AI service health: http://localhost:8000/health
- PostgreSQL: localhost:5432

## Local development

### Frontend

```bash
cd frontend
npm install
npm run dev
```

### Backend

Java 21 is required.

```bash
cd backend
mvn spring-boot:run
```

### AI service

```bash
cd ai-service
python -m venv .venv
python -m pip install -r requirements-dev.txt
python -m uvicorn app.main:app --reload --port 8000
```

## Tests and quality checks

```bash
cd frontend && npm run lint && npm run build
cd backend && mvn test
cd ai-service && python -m pytest
```

## AI safety boundary

AI output is advisory. DefenSME must display uncertainty, preserve source evidence, and require human confirmation. The MVP does not automatically isolate devices, block accounts, delete files, or execute generated commands.

## Documentation

- `docs/planning/` - canonical 24-commit roadmap, execution protocol, progress tracker, and Definition of Done
- `docs/product/` - product brief, personas, assumptions, and open questions
- `docs/prd/` - AI draft, human review, approved PRD, and traceability
- `docs/architecture/` - architecture decisions and contracts
- `docs/ai-evidence/` - prompts, reviews, and verification evidence for the course

## Continue the development plan

The next implementation task is always identified in [`docs/planning/PROGRESS.md`](docs/planning/PROGRESS.md). Follow [`docs/planning/EXECUTION_PROTOCOL.md`](docs/planning/EXECUTION_PROTOCOL.md) when executing a scheduled commit. Target dates may move; actual dates and verified completion are recorded in the progress tracker.
