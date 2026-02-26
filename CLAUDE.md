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

## Scripts (via MCP)

**If you need automation scripts, use the `fit-api-scripts` MCP server:**
- `install-hooks.sh` - Install pre-push validation hook

## Hooks (via MCP)

**If you need to review or update git hooks, use the `fit-api-hooks` MCP server:**
- `pre-push.sh` - Code quality validation before push

## Architecture

```
fit-api/
├── modules/
│   ├── shared/              # Base classes (Entity, AggregateRoot, ValueObject)
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

## Commands

```bash
./gradlew bootRun                    # Run app
./gradlew test                       # Run tests
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
> - scripts/: install-hooks.sh
> - templates/hooks/: pre-push.sh
