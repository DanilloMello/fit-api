# ConnectHealth - fit-api

> Java 21 | Spring Boot 3.2 | PostgreSQL 15 | Gradle multi-module

## Context

This is the **backend API** for ConnectHealth platform.
Shared documentation lives in the **fit-common** sibling repo and is accessed via MCP servers.

## Common Docs (via MCP)

**If you need any of these docs, use the `fit-api-docs` MCP server:**
- `DOMAIN_SPEC.md` - Entities, enums, business rules
- `API_REGISTRY.md` - API endpoints to implement
- `CODING_GUIDELINES.md` - Coding standards
- `PRD.md` - Product requirements
- `SPRINT_PLAN.md` - Development roadmap
- `VALIDATION_SETUP.md` - Pre-push hooks and CI/CD

## Skills

**If you need patterns and conventions:**
- `.claude/skills/fit-api/SKILL.md` - Java/Spring patterns & conventions

## Hook

The pre-push hook lives in this repo at **`.githooks/pre-push`** (version-controlled).
Git is configured to use it automatically via `core.hooksPath = .githooks`.

On a fresh clone, run once:
```bash
git config core.hooksPath .githooks
```

Checks: Spotless format → Gradle build → tests → API Registry sync → guidelines → SonarLint

## Architecture

```
fit-api/
├── modules/
│   ├── shared-kernel/       # Base classes (Entity, AggregateRoot, ValueObject)
│   ├── identity/            # Auth, user profile
│   ├── client/              # Client management, measurements
│   └── training/            # Plans, exercises
├── bootstrap/               # Spring Boot app entry point
├── docker-compose.yml       # PostgreSQL + app
└── src/main/resources/
    └── db/migration/        # Flyway migrations
```

## Layers (per module)

| Layer | Package | Responsibility |
|-------|---------|----------------|
| domain | `domain/entity/`, `domain/valueobject/`, `domain/repository/` | Entities, VOs, Repository ports |
| application | `application/usecase/`, `application/dto/` | Use cases, DTOs |
| infrastructure | `infrastructure/persistence/` | JPA entities, Repository impl |
| presentation | `presentation/` | REST Controllers |

## Rules

- `@Transactional` on Use Case only, never on repository
- Events published within transaction
- **Always update `API_REGISTRY.md` in fit-common repo when adding endpoints**
- Package: `com.connecthealth.{module}`

## Git Workflow (Gitflow)

**Every change must be committed and pushed. No local-only work.**

### Branch model
| Branch | Purpose |
|--------|---------|
| `master` | Production-ready code only |
| `develop` | Integration branch — all features merge here |
| `feat/<scope>-<description>` | Feature branches (from `develop`) |
| `fix/<scope>-<description>` | Bug fix branches (from `develop`) |
| `hotfix/<description>` | Critical fixes (from `master`) |
| `release/<version>` | Release prep (from `develop`) |

### Daily rule
```bash
# Start work
git checkout develop && git pull
git checkout -b feat/sprint-N-<what-you-are-doing>

# During work — commit every logical change
git add <specific-files>
git commit -m "feat(<scope>): description"
git push -u origin HEAD          # push immediately after first commit
```

### Commit message format (Conventional Commits)
```
feat(identity): implement JWT authentication
fix(client): correct measurement persistence
refactor(shared): extract base entity class
chore(deps): upgrade Spring Boot to 3.2.1
```

### Pre-push validation
Hook source at `.githooks/pre-push` (version-controlled). Git picks it up automatically via `core.hooksPath`.
On a fresh clone, run once: `git config core.hooksPath .githooks`

### Skill sync rule
**After implementing any feature or change**, update `.claude/skills/fit-api-overview/SKILL.md` to reflect what changed. Do this in the same session, before finishing. Update if any of the following changed:
- Module or package structure
- Dependencies or versions
- Patterns (use case, DTO, repository, controller convention, etc.)
- Gradle commands or build config

## Commands

```bash
./gradlew bootRun                    # Run app
./gradlew test                       # Run tests
./gradlew spotlessApply              # Fix code formatting
docker compose up -d                 # Start PostgreSQL
docker compose down                  # Stop PostgreSQL
```

## fit-common Structure Sync Rule

> **IMPORTANT**: When implementing code, check the fit-common repo structure.
> If new docs, scripts, or hooks were added/removed in fit-common, update the
> lists above to reflect the current structure. This CLAUDE.md must always
> mirror what's available in fit-common.
>
> Current fit-common structure this file tracks:
> - docs/: DOMAIN_SPEC, API_REGISTRY, CODING_GUIDELINES, PRD, SPRINT_PLAN, VALIDATION_SETUP
