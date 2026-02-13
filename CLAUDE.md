# ConnectHealth - fit-api

> Java 21 | Spring Boot 3.2 | PostgreSQL 15 | Gradle multi-module

## Context

This is the **backend API** for ConnectHealth platform. For full project documentation, see `../fit-common/`.

## Docs (multi-repo)

Before working, read these docs from `../fit-common/`:

1. `docs/DOMAIN_SPEC.md` - Entities, enums, business rules
2. `docs/API_REGISTRY.md` - API endpoints to implement
3. `skills/fit-api/SKILL.md` - Java/Spring patterns & conventions
4. `fit-api/ARCHITECTURE.md` - Module structure & layers
5. `fit-api/DATABASE.md` - PostgreSQL schema & migrations

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
- **Always update `../fit-common/docs/API_REGISTRY.md` when adding endpoints**
- Package: `com.connecthealth.{module}`

## Sprint Plan

See `../fit-common/docs/SPRINT_PLAN.md` for roadmap.

## Commands

```bash
./gradlew bootRun                    # Run app
./gradlew test                       # Run tests
docker compose up -d                 # Start PostgreSQL
docker compose down                  # Stop PostgreSQL
```
