---
name: fit-api-overview
description: Backend skill for fit-api. Java/Spring patterns and conventions.
---

# fit-api Skill

> **Prereq**: Use `fit-api-docs` MCP to read `DOMAIN_SPEC.md` first

---

## 1. File Locations

| Creating | Path |
|----------|------|
| Model (JPA Entity) | `modules/{m}/model/{Entity}.java` |
| DTO | `modules/{m}/dto/{Name}.java` |
| Repository | `modules/{m}/repository/{Entity}Repository.java` |
| Service | `modules/{m}/service/{Entity}Service.java` |
| Controller | `modules/{m}/controller/{Entity}Controller.java` |
| Migration | `src/main/resources/db/migration/V{n}__{m}_{action}.sql` |

---

## 2. Modules

| Module | Package | Entities |
|--------|---------|----------|
| identity | `com.connecthealth.identity` | User |
| client | `com.connecthealth.client` | Client, Measurement |
| training | `com.connecthealth.training` | Plan, Exercise, PlanExercise |

---

## 3. Patterns

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

    protected Client() {}

    public Client(UUID ownerId, String name) {
        this.ownerId = ownerId;
        this.name = name;
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

### DTO
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

## 4. Rules

- `@Transactional` on Service only — never on Repository or Controller
- Use `@Transactional(readOnly = true)` for query-only methods
- Controllers use DTOs — never expose JPA entities directly
- No business logic in controllers — delegate to Services
- `ApiResponse<T>` from `com.connecthealth.shared.dto` wraps all responses
- **Update `API_REGISTRY.md` in fit-common repo when adding endpoints**

---

## 5. Checklist: New Entity

- [ ] Model in `model/`
- [ ] Repository in `repository/` extending `JpaRepository`
- [ ] Migration file in `db/migration/`

## 6. Checklist: New Endpoint

- [ ] Service method with `@Transactional`
- [ ] Controller method
- [ ] Request/Response DTOs
- [ ] **Update `docs/API_REGISTRY.md`**
