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
│   ├── identity/      # User, Auth, JWT, Security
│   │   └── src/main/java/com/connecthealth/identity/
│   │       ├── controller/   # AuthController, ProfileController
│   │       ├── dto/          # Request/Response records
│   │       ├── exception/    # ApiException hierarchy, GlobalExceptionHandler
│   │       ├── model/        # User entity
│   │       ├── repository/   # UserRepository
│   │       ├── security/     # JwtService, JwtAuthFilter, SecurityConfig, UserPrincipal
│   │       └── service/      # AuthService, ProfileService
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
| Security | `modules/identity/src/.../security/` | `@Configuration`, `@Service`, `@Component` |
| Exception | `modules/identity/src/.../exception/` | `@RestControllerAdvice` |

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
| [identity/exception/GlobalExceptionHandler.java](../../../modules/identity/src/main/java/com/connecthealth/identity/exception/GlobalExceptionHandler.java) | Error response format for all modules |
| [identity/security/SecurityConfig.java](../../../modules/identity/src/main/java/com/connecthealth/identity/security/SecurityConfig.java) | Security filter chain, public routes |
| [bootstrap/build.gradle](../../../bootstrap/build.gradle) | App-level deps (web, jpa, flyway, validation) |
| [bootstrap/application.yml](../../../bootstrap/src/main/resources/application.yml) | DB, Flyway, JPA, JWT config |
| [identity/build.gradle](../../../modules/identity/build.gradle) | Module-level deps (security, JWT) |

---

## Patterns

### Model (JPA Entity)
```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected User() {}

    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    // getters and setters — no setId, no setCreatedAt
}
```

### Repository
```java
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByRefreshToken(String refreshToken);
}
```

### Service
```java
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(...) { ... }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already in use");
        }
        User user = new User(req.name(), req.email(), passwordEncoder.encode(req.password()));
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public SomeResponse query(UUID id) { ... }
}
```

### Controller
```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        AuthResponse response = authService.register(req);
        return ResponseEntity.status(201).body(ApiResponse.success(response));
    }

    // Protected endpoint — inject @AuthenticationPrincipal
    @GetMapping
    ResponseEntity<ApiResponse<UserResponse>> getProfile(@AuthenticationPrincipal UserPrincipal user) {
        UserResponse response = profileService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

### DTO (record)
```java
public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password
) {}

public record AuthResponse(UserResponse user, TokenResponse tokens) {}

public record TokenResponse(String accessToken, String refreshToken, long expiresIn) {}

public record UserResponse(UUID id, String name, String email, String phone, String photoUrl, LocalDateTime createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getPhone(), user.getPhotoUrl(), user.getCreatedAt());
    }
}
```

### Exception hierarchy
```java
// Base exception — carries HTTP status and error code
public class ApiException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public ApiException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }
    // getters
}

// Convenience subclasses
public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super("CONFLICT", message, HttpStatus.CONFLICT);
    }
}

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }
}

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }
}
```

### Error response format (GlobalExceptionHandler)

Errors do NOT use `ApiResponse<T>`. They use a separate envelope:

```json
{ "error": { "code": "CONFLICT", "message": "Email already in use", "details": null } }
{ "error": { "code": "VALIDATION_ERROR", "message": "Validation failed", "details": { "email": "must be a well-formed email address" } } }
```

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    record ErrorDetail(String code, String message, Map<String, String> details) {}
    record ErrorResponse(ErrorDetail error) {}

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorResponse(new ErrorDetail(ex.getCode(), ex.getMessage(), null)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField,
                        f -> f.getDefaultMessage() != null ? f.getDefaultMessage() : "invalid"));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(new ErrorDetail("VALIDATION_ERROR", "Validation failed", details)));
    }
}
```

### Security — UserPrincipal
```java
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) { this.user = user; }

    public UUID getId() { return user.getId(); }

    @Override public String getUsername() { return user.getEmail(); }
    @Override public String getPassword() { return user.getPasswordHash(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    // isAccountNonExpired, isAccountNonLocked, isCredentialsNonExpired, isEnabled → all return true
}
```

### Security — SecurityConfig (public routes)
```java
.authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/v1/auth/**", "/api/v1/health").permitAll()
        .anyRequest().authenticated()
)
```
Add new public routes to the `permitAll()` matcher. All other routes require a valid JWT.

---

## Testing

### Module — @WebMvcTest slice setup

Each module needs a minimal `*TestApplication` as the `@SpringBootConfiguration` anchor:

```java
// modules/identity/src/test/java/com/connecthealth/identity/IdentityTestApplication.java
@SpringBootApplication(
        scanBasePackages = "com.connecthealth.identity",
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class}
)
class IdentityTestApplication {}
```

### Controller test
```java
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)          // pulls in the security filter chain
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    @MockBean JwtService jwtService;           // security filter dep
    @MockBean UserDetailsServiceImpl userDetailsService; // security filter dep

    @Test
    void register_validRequest_returns201() throws Exception {
        when(authService.register(any())).thenReturn(stubAuthResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Alice","email":"alice@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.email").value("alice@example.com"))
                .andExpect(jsonPath("$.data.tokens.accessToken").value("access"));
    }
}
```

For **protected endpoints** that inject `@AuthenticationPrincipal UserPrincipal`, add `.with(user(principal))`:
```java
mockMvc.perform(get("/api/v1/profile")
        .with(user(new UserPrincipal(someUser))))
        .andExpect(status().isOk());
```

### Integration test (bootstrap)
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void registerAndLoginFlow_returnsTokens() throws Exception {
        // Register → Login (rotates refresh token) → Refresh → use new access token
    }
}
```

**Never** use `Thread.sleep` in tests — test expired tokens by setting expiration to `-1L` via `ReflectionTestUtils`.

---

## Rules

- `@Transactional` on Service only — never on Repository or Controller
- `@Transactional(readOnly = true)` on all query-only service methods
- Controllers use DTOs — never expose JPA entities directly
- No business logic in controllers — delegate to Services
- `ApiResponse<T>` wraps all **success** responses; `ErrorResponse` wraps all **error** responses
- Throw `ApiException` subclasses from Services — `GlobalExceptionHandler` converts them automatically
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
- [ ] Throw `ApiException` subclass (not raw exceptions) for error cases
- [ ] If public route: add to `permitAll()` in `SecurityConfig`
- [ ] Update `API_REGISTRY.md` in fit-common

### New Module
- [ ] Create `{module}TestApplication` in `src/test` (excludes DataSource/JPA auto-configs)
- [ ] Add `exception/` package or reuse identity's `GlobalExceptionHandler` if shared
