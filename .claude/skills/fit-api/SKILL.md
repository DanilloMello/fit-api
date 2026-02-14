---
name: fit-api-skill
description: Backend skill for fit-api. Java/Spring patterns and conventions.
---

# fit-api Skill

> **Prereq**: Use `fit-api-docs` MCP to read `DOMAIN_SPEC.md` first

---

## 1. File Locations

| Creating | Path |
|----------|------|
| Entity | `modules/{m}/domain/entity/{Entity}.java` |
| Value Object | `modules/{m}/domain/valueobject/{Name}.java` |
| Repository Port | `modules/{m}/domain/repository/{Entity}Repository.java` |
| Use Case | `modules/{m}/application/usecase/{Action}{Entity}UseCase.java` |
| DTO | `modules/{m}/application/dto/{Name}.java` |
| JPA Entity | `modules/{m}/infrastructure/persistence/{Entity}JpaEntity.java` |
| Repository Impl | `modules/{m}/infrastructure/persistence/{Entity}RepositoryImpl.java` |
| Controller | `modules/{m}/presentation/{Entity}Controller.java` |
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

### Entity
```java
public class Client extends AggregateRoot<ClientId> {
    private Client() {}
    public static Client create(UserId ownerId, String name) {
        Client c = new Client();
        c.id = ClientId.generate();
        c.ownerId = ownerId;
        c.name = name;
        c.registerEvent(new ClientCreatedEvent(c.id));
        return c;
    }
}
```

### Use Case
```java
@Service
@Transactional
public class CreateClientUseCase {
    public ClientResponse execute(CreateClientCommand cmd) {
        Client c = Client.create(cmd.ownerId(), cmd.name());
        clientRepository.save(c);
        eventPublisher.publishAll(c.getDomainEvents());
        return ClientResponse.from(c);
    }
}
```

### Controller
```java
@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {
    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponse>> create(
            @Valid @RequestBody ClientCreateRequest req,
            @AuthenticationPrincipal UserPrincipal user) {
        var cmd = new CreateClientCommand(UserId.of(user.getId()), req.name());
        return ResponseEntity.status(201).body(ApiResponse.success(useCase.execute(cmd)));
    }
}
```

---

## 4. Rules

- `@Transactional` on Use Case only
- **Update `API_REGISTRY.md` in fit-common repo when adding endpoints**

---

## 5. Checklist: New Entity

- [ ] Entity in `domain/entity/`
- [ ] Repository port in `domain/repository/`
- [ ] JPA entity in `infrastructure/persistence/`
- [ ] Repository impl
- [ ] Migration file

## 6. Checklist: New Endpoint

- [ ] Use case
- [ ] Controller method
- [ ] **Update `docs/API_REGISTRY.md`**
