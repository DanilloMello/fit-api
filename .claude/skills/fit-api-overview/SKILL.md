---
name: fit-api-overview
description: Backend skill for fit-api. Java/Spring patterns and conventions.
---

# fit-api — Entry Point

> Start here whenever you need to understand, plan, or implement anything in this project.

---

## Step 1 — Fetch Docs from fit-common

Before any task, pull the relevant docs. All shared specs live at `DanilloMello/fit-common`.

```bash
# Common (all platforms)
gh api repos/DanilloMello/fit-common/contents/docs/common/DOMAIN_SPEC.md --jq '.content' | base64 -d
gh api repos/DanilloMello/fit-common/contents/docs/common/SPRINT_PLAN.md --jq '.content' | base64 -d
gh api repos/DanilloMello/fit-common/contents/docs/common/API_REGISTRY.md --jq '.content' | base64 -d

# Server-specific
gh api repos/DanilloMello/fit-common/contents/docs/server/CODING_GUIDELINES.md --jq '.content' | base64 -d
```

| Doc | When to read |
|-----|-------------|
| `DOMAIN_SPEC.md` | Entity fields, enums, business rules |
| `SPRINT_PLAN.md` | What sprint we're on and what's in scope |
| `API_REGISTRY.md` | Endpoints to implement or update |
| `server/CODING_GUIDELINES.md` | Java/Spring code standards |

---

## Step 2 — Explore the Project

Use **Glob** and **Read** directly. Never use a Task agent just to explore.

```
# Find all source files in a module
Glob: modules/identity/src/**/*.java

# Find all migrations
Glob: bootstrap/src/main/resources/db/migration/*.sql

# Find a specific class
Grep: "class AuthService" (type: java)
```

### Project layout

```
fit-api/
├── modules/
│   ├── shared/        # ApiResponse<T>
│   ├── identity/      # User, Auth, JWT
│   ├── client/        # Client, Measurement
│   └── training/      # Plan, Exercise, PlanExercise
└── bootstrap/
    ├── src/main/java/com/connecthealth/bootstrap/
    │   └── ConnectHealthApplication.java   # @SpringBootApplication
    └── src/main/resources/
        ├── application.yml
        ├── application-dev.yml
        └── db/migration/                   # Flyway — V{n}__{module}_{action}.sql
```

### Layers (per module)

| Layer | Path | Annotation |
|-------|------|-----------|
| Model | `modules/{m}/src/main/java/com/connecthealth/{m}/model/` | `@Entity` |
| DTO | `modules/{m}/src/main/java/com/connecthealth/{m}/dto/` | `record` |
| Repository | `modules/{m}/src/main/java/com/connecthealth/{m}/repository/` | `JpaRepository` |
| Service | `modules/{m}/src/main/java/com/connecthealth/{m}/service/` | `@Service` |
| Controller | `modules/{m}/src/main/java/com/connecthealth/{m}/controller/` | `@RestController` |

### Modules and packages

| Module | Package | Entities |
|--------|---------|----------|
| identity | `com.connecthealth.identity` | User |
| client | `com.connecthealth.client` | Client, Measurement |
| training | `com.connecthealth.training` | Plan, Exercise, PlanExercise |

---

## Step 3 — Key Files to Read

Read these early in any session — they define project-wide contracts.

| File | Purpose |
|------|---------|
| [shared/dto/ApiResponse.java](../../../modules/shared/src/main/java/com/connecthealth/shared/dto/ApiResponse.java) | Response wrapper used in all controllers |
| [bootstrap/build.gradle](../../../bootstrap/build.gradle) | App-level deps (web, jpa, flyway, validation) |
| [bootstrap/application.yml](../../../bootstrap/src/main/resources/application.yml) | DB, Flyway, JPA config |
| [identity/build.gradle](../../../modules/identity/build.gradle) | Module-level deps (security, JWT) |

---

## Patterns

### Model (JPA Entity)
```java
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Client() {}

    public Client(UUID ownerId, String name) {
        this.ownerId = ownerId;
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    // getters and setters
}
```

### Repository
```java
public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findByOwnerId(UUID ownerId);
}
```

### Service
```java
@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public ClientResponse create(UUID ownerId, CreateClientRequest req) {
        Client client = new Client(ownerId, req.name());
        clientRepository.save(client);
        return ClientResponse.from(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> listByOwner(UUID ownerId) {
        return clientRepository.findByOwnerId(ownerId)
                .stream()
                .map(ClientResponse::from)
                .toList();
    }
}
```

### Controller
```java
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponse>> create(
            @Valid @RequestBody CreateClientRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        ClientResponse response = clientService.create(user.getId(), req);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClientResponse>>> list(
            @AuthenticationPrincipal UserPrincipal user) {
        List<ClientResponse> clients = clientService.listByOwner(user.getId());
        return ResponseEntity.ok(ApiResponse.success(clients));
    }
}
```

### DTO (record)
```java
public record CreateClientRequest(
        @NotBlank String name
) {}

public record ClientResponse(UUID id, UUID ownerId, String name) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(client.getId(), client.getOwnerId(), client.getName());
    }
}
```

---

## Rules

- `@Transactional` on Service only — never on Repository or Controller
- `@Transactional(readOnly = true)` on all query-only service methods
- Controllers use DTOs — never expose JPA entities directly
- No business logic in controllers — delegate to Services
- `ApiResponse<T>` from `com.connecthealth.shared.dto` wraps all responses
- Update `API_REGISTRY.md` in fit-common when adding or changing endpoints
- When adding a dependency, use the `context7` MCP to check the latest stable version

---

## Checklists

### New Entity
- [ ] Model in `model/` with protected no-arg constructor
- [ ] Repository in `repository/` extending `JpaRepository`
- [ ] Flyway migration `V{n}__{module}_{action}.sql` in `bootstrap/src/main/resources/db/migration/`

### New Endpoint
- [ ] Request/Response DTOs in `dto/`
- [ ] Service method with `@Transactional`
- [ ] Controller method returning `ResponseEntity<ApiResponse<T>>`
- [ ] Update `API_REGISTRY.md` in fit-common
