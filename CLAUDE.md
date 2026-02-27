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

## Testing Rules

**Every new class must have a corresponding test. Always maintain 100% coverage.**

### Test types per layer

| Layer | Test type | Tool |
|-------|-----------|------|
| model / dto / security utilities | Unit test (`@ExtendWith(MockitoExtension.class)`) | Module test sources |
| service | Unit test with `@Mock` dependencies | Module test sources |
| controller | `@WebMvcTest` with `@MockBean` services | Module test sources |
| full use-case flow | Integration test (`@SpringBootTest` + Testcontainers) | `bootstrap` test sources |

### Checklist when adding a new class

- [ ] Unit test covers all methods and branches (happy path + error paths)
- [ ] Service test mocks all repository/external dependencies
- [ ] Controller test covers: success response, validation error (400), and expected error statuses
- [ ] New endpoint covered by an integration test scenario in `bootstrap/src/test`

### Conventions

- Test class mirrors the production class: `Foo` → `FooTest` in the same package under `src/test/java`
- Controller tests that need `@AuthenticationPrincipal UserPrincipal` use `.with(user(principal))` from `SecurityMockMvcRequestPostProcessors`
- `@WebMvcTest` in module tests needs `@MockBean JwtService` and `@MockBean UserDetailsServiceImpl` (security filter deps)
- Integration tests use `@Testcontainers` + `@Container @ServiceConnection PostgreSQLContainer`
- Never use `Thread.sleep` in tests — test expired tokens by setting expiration to `-1L` via `ReflectionTestUtils`

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
