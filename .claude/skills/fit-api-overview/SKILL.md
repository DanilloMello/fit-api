---
name: fit-api-overview
description: Backend skill for fit-api. Java/Spring patterns and conventions.
---

# fit-api Overview

> **Prereq**: Use `fit-api-docs` MCP to read `DOMAIN_SPEC.md` first

---

## 1. File Locations

| Creating | Path |
|----------|------|
| Model (JPA Entity) | `modules/{m}/src/main/java/com/connecthealth/{m}/model/{Entity}.java` |
| Repository | `modules/{m}/src/main/java/com/connecthealth/{m}/repository/{Entity}Repository.java` |
| Service | `modules/{m}/src/main/java/com/connecthealth/{m}/service/{Entity}Service.java` |
| Controller | `modules/{m}/src/main/java/com/connecthealth/{m}/controller/{Entity}Controller.java` |
| Request DTO | `modules/{m}/src/main/java/com/connecthealth/{m}/dto/request/{Action}Request.java` |
| Response DTO | `modules/{m}/src/main/java/com/connecthealth/{m}/dto/{Name}Response.java` |
| Exception | `modules/{m}/src/main/java/com/connecthealth/{m}/exception/{Name}Exception.java` |
| Migration | `bootstrap/src/main/resources/db/migration/V{n}__{m}_{action}.sql` |

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
    private UUID id;

    @Column(nullable = false)
    private String name;

    protected Client() {}

    public Client(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    // getters only
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

    public ClientResponse create(CreateClientRequest request) {
        Client client = new Client(UUID.randomUUID(), request.name());
        clientRepository.save(client);
        return new ClientResponse(client.getId().toString(), client.getName());
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(UUID id) { ... }
}
```

### Controller
```java
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
        return ApiResponse.success(clientService.create(request));
    }
}
```

### Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SomeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handle(SomeException ex) {
        return new ErrorResponse(new ErrorDto("ERROR_CODE", ex.getMessage()));
    }
}
```

---

## 4. Rules

- `@Transactional` on Service methods only
- Read-only queries use `@Transactional(readOnly = true)`
- **Update `API_REGISTRY.md` in fit-common repo when adding endpoints**
- Email normalization: always `.toLowerCase()` before save/query

---

## 5. Checklist: New Entity

- [ ] Model in `model/` with `@Entity`
- [ ] Repository extending `JpaRepository`
- [ ] Service with business logic
- [ ] Migration file in `bootstrap/src/main/resources/db/migration/`

## 6. Checklist: New Endpoint

- [ ] Request DTO in `dto/request/`
- [ ] Service method
- [ ] Controller method
- [ ] **Update `docs/API_REGISTRY.md`**
