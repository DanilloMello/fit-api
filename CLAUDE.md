# ConnectHealth - fit-api

> Java 21 | Spring Boot 3.2 | PostgreSQL 15 | Gradle multi-module

## Context

This is the **backend API** for ConnectHealth platform.
Shared documentation lives in the **fit-common** GitHub repo: `DanilloMello/fit-common`.

## Common Docs (via GitHub)

**IMPORTANT: Before starting any task, fetch the relevant docs from GitHub to get context and policies.**

### Common docs (all platforms)
```bash
gh api repos/DanilloMello/fit-common/contents/docs/common/<FILE> --jq '.content' | base64 -d
```
- `DOMAIN_SPEC.md` - Entities, enums, business rules
- `API_REGISTRY.md` - API endpoints to implement
- `CODING_GUIDELINES.md` - Coding standards
- `PRD.md` - Product requirements
- `SPRINT_PLAN.md` - Development roadmap
- `VALIDATION_SETUP.md` - Pre-push hooks and CI/CD

### Server-specific docs
```bash
gh api repos/DanilloMello/fit-common/contents/docs/server/<FILE> --jq '.content' | base64 -d
```
- `CODING_GUIDELINES.md` - Server coding standards
- `VALIDATION_SETUP.md` - Server validation setup

To list available docs:
```bash
gh api repos/DanilloMello/fit-common/contents/docs/common --jq '.[].name'
gh api repos/DanilloMello/fit-common/contents/docs/server --jq '.[].name'
```

## Skills

**If you need patterns and conventions:**
- `.claude/skills/fit-api-overview/SKILL.md` - Java/Spring patterns & conventions

## Architecture

```
fit-api/
├── modules/
│   ├── shared/              # Shared DTOs (ApiResponse)
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
| model | `model/` | JPA entities (`@Entity`) |
| dto | `dto/` | Request/Response DTOs |
| repository | `repository/` | Spring Data JPA repositories |
| service | `service/` | Business logic (`@Service`, `@Transactional`) |
| controller | `controller/` | REST Controllers (`@RestController`) |

## Rules

- `@Transactional` on Service only, never on Repository or Controller
- Use `@Transactional(readOnly = true)` for query-only service methods
- Controllers use DTOs — never expose JPA entities directly
- No business logic in controllers — delegate to Services
- `ApiResponse<T>` from `com.connecthealth.shared.dto` wraps all responses
- **Always update `API_REGISTRY.md` in fit-common repo when adding endpoints**
- **When adding or updating a dependency, use the `context7` MCP to check the latest stable version**
- Package: `com.connecthealth.{module}`

## Commands

```bash
./gradlew bootRun                    # Run app
./gradlew test                       # Run tests
docker compose up -d                 # Start PostgreSQL
docker compose down                  # Stop PostgreSQL
```
