# Sprint 1: Identity — fit-api

> **Generated:** 2026-02-17
> **Sprint:** 1
> **Project:** fit-api
> **Status:** In Progress

---

## Sprint Overview

Implement the full Identity bounded context: User entity, JWT-based authentication (register, login, refresh), and the user profile endpoints. This sprint delivers the authentication foundation that all subsequent sprints depend on.

---

## Progress

| Task | Status | Priority |
|------|--------|----------|
| Task 1: User domain entity | ⬜ Not started | High |
| Task 2: Flyway migration V1 — users table | ⬜ Not started | High |
| Task 3: JPA entity + repository | ⬜ Not started | High |
| Task 4: Register use case | ⬜ Not started | High |
| Task 5: Login + JWT use case | ⬜ Not started | High |
| Task 6: Refresh token use case | ⬜ Not started | High |
| Task 7: Auth controller (POST /auth/register, /auth/login, /auth/refresh) | ⬜ Not started | High |
| Task 8: Spring Security configuration | ⬜ Not started | High |
| Task 9: JWT infrastructure (generate, validate, parse) | ⬜ Not started | High |
| Task 10: Tests | ⬜ Not started | High |

---

## Tasks

---

### Task 1: User domain entity

**Priority:** High
**Size:** S
**Depends on:** nothing

> Model the User aggregate root in the domain layer. No framework dependencies — pure Java.

#### Subtasks

- [ ] **1.1** — Create `User.java` in `modules/identity/src/main/java/com/connecthealth/identity/domain/entity/`
  - Extend `AggregateRoot<UserId>` from `shared`
  - Fields: `id (UserId)`, `name (String)`, `email (Email)`, `passwordHash (String)`, `phone (String, nullable)`, `photoUrl (String, nullable)`, `createdAt (Instant)`
  - Constructor validates: name not blank, email not null, passwordHash not blank
  - No JPA annotations — this is pure domain

- [ ] **1.2** — Create `UserId.java` (Value Object) in `domain/valueobject/`
  - Wraps `UUID`
  - Factory method: `UserId.generate()` → `new UserId(UUID.randomUUID())`
  - Factory method: `UserId.of(UUID uuid)`
  - Override `equals` and `hashCode`

- [ ] **1.3** — Create `Email.java` (Value Object) in `domain/valueobject/`
  - Wraps `String`
  - Constructor validates format with regex or `jakarta.validation` pattern: `^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$`
  - Throw `IllegalArgumentException("Invalid email format")` on invalid input
  - `getValue()` returns the raw string

- [ ] **1.4** — Create `UserRepository.java` (port) in `domain/repository/`
  - Interface — implemented by infrastructure layer
  - Methods: `save(User user)`, `findById(UserId id): Optional<User>`, `findByEmail(Email email): Optional<User>`, `existsByEmail(Email email): boolean`

#### Acceptance Criteria

- [ ] `User` is instantiable with valid fields and throws on invalid
- [ ] `Email` rejects `"notanemail"` and accepts `"joao@email.com"`
- [ ] `UserRepository` is an interface with no Spring/JPA imports
- [ ] Unit tests pass for `User`, `Email`, `UserId`

---

### Task 2: Flyway migration V1 — users table

**Priority:** High
**Size:** S
**Depends on:** nothing (can run in parallel with Task 1)

> Create the database schema for the users table.

#### Subtasks

- [ ] **2.1** — Locate the migrations directory
  - Path: `bootstrap/src/main/resources/db/migration/` (or `modules/identity/src/main/resources/db/migration/` — check `application.yml` for `spring.flyway.locations`)
  - If directory doesn't exist, create it

- [ ] **2.2** — Create `V1__create_users_table.sql`
  ```sql
  CREATE TABLE users (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone       VARCHAR(50),
    photo_url   TEXT,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
  );

  CREATE INDEX idx_users_email ON users(email);
  ```
  - Use `UUID` not `SERIAL` (domain uses UUID identity)
  - `email` has `UNIQUE` constraint
  - `created_at` defaults to `NOW()`

- [ ] **2.3** — Verify Flyway dependency exists in `bootstrap/build.gradle` or `build.gradle`
  - Should have `implementation 'org.flywaydb:flyway-core'` and `implementation 'org.flywaydb:flyway-database-postgresql'`
  - Add if missing

#### Acceptance Criteria

- [ ] Running `docker compose up -d && ./gradlew bootRun` applies V1 migration without errors
- [ ] Table `users` exists in `connecthealth` database with correct columns

---

### Task 3: JPA entity + repository implementation

**Priority:** High
**Size:** M
**Depends on:** Task 1, Task 2

> Bridge between domain and database: JPA entity maps to the `users` table, repository implementation converts between JPA and domain.

#### Subtasks

- [ ] **3.1** — Create `UserJpaEntity.java` in `modules/identity/src/main/java/com/connecthealth/identity/infrastructure/persistence/`
  - Annotate with `@Entity`, `@Table(name = "users")`
  - Fields match `V1__create_users_table.sql` columns (snake_case → camelCase via `@Column`)
  - `id` field: `@Id`, type `UUID`
  - `createdAt`: `@Column(name = "created_at")`, type `Instant`
  - No domain logic — just a data holder

- [ ] **3.2** — Create `UserJpaRepository.java` (Spring Data interface) in same package
  - Extends `JpaRepository<UserJpaEntity, UUID>`
  - Add: `Optional<UserJpaEntity> findByEmail(String email);`
  - Add: `boolean existsByEmail(String email);`

- [ ] **3.3** — Create `UserRepositoryImpl.java` implementing `UserRepository` (domain port)
  - Inject `UserJpaRepository`
  - `save(User user)`: map domain → JPA entity → call `jpaRepository.save()` → map JPA → domain → return
  - `findById(UserId id)`: call `jpaRepository.findById(id.getValue())` → map to domain if present
  - `findByEmail(Email email)`: call `jpaRepository.findByEmail(email.getValue())` → map
  - `existsByEmail(Email email)`: delegate directly
  - Mapper methods can be private static methods within the same class

- [ ] **3.4** — Register `UserRepositoryImpl` as a Spring `@Repository` bean
  - Annotate with `@Repository` and `@Component` is redundant — use `@Repository` only

#### Acceptance Criteria

- [ ] `UserRepositoryImpl` implements `UserRepository` interface from domain layer
- [ ] Saving a `User` and finding it by ID/email works end-to-end with a real DB (test with Testcontainers in Task 10)
- [ ] No domain imports in `UserJpaEntity`

---

### Task 4: Register use case

**Priority:** High
**Size:** M
**Depends on:** Task 1, Task 3

> Handles new user registration: validates uniqueness, hashes password, persists user, returns tokens.

#### Subtasks

- [ ] **4.1** — Create `RegisterUserUseCase.java` in `modules/identity/src/main/java/com/connecthealth/identity/application/usecase/`
  - Annotate with `@Service` and `@Transactional`
  - Constructor-inject: `UserRepository`, `PasswordEncoder` (Spring Security), `JwtService` (Task 9)
  - Method signature: `AuthTokensDto execute(RegisterUserCommand command)`

- [ ] **4.2** — Create `RegisterUserCommand.java` in `application/usecase/` (or `application/dto/`)
  - Record or immutable class with: `String name`, `String email`, `String password`
  - No validation annotations here — validated at controller boundary

- [ ] **4.3** — Implement `execute` logic in `RegisterUserUseCase`:
  1. Check `userRepository.existsByEmail(new Email(command.email()))` → if true, throw `EmailAlreadyExistsException`
  2. Hash password: `passwordEncoder.encode(command.password())`
  3. Create domain entity: `new User(UserId.generate(), command.name(), new Email(command.email()), passwordHash, ...)`
  4. Save: `userRepository.save(user)`
  5. Generate tokens: `jwtService.generateTokens(user.getId(), user.getEmail())`
  6. Return `AuthTokensDto(user, accessToken, refreshToken, expiresIn: 900)`

- [ ] **4.4** — Create `EmailAlreadyExistsException.java` in `domain/exception/`
  - Extends `RuntimeException`
  - Message: `"Email already registered: " + email`
  - This will be mapped to HTTP 409 Conflict in the controller/exception handler

- [ ] **4.5** — Create `AuthTokensDto.java` in `application/dto/`
  - Record: `UserDto user`, `String accessToken`, `String refreshToken`, `int expiresIn`
  - Create `UserDto` record: `String id`, `String name`, `String email`

#### Acceptance Criteria

- [ ] Calling `execute` with a new email creates a user and returns tokens
- [ ] Calling `execute` with a duplicate email throws `EmailAlreadyExistsException`
- [ ] Password stored as bcrypt hash (never plain text)
- [ ] `@Transactional` is on the use case, not the repository

---

### Task 5: Login use case

**Priority:** High
**Size:** S
**Depends on:** Task 1, Task 3, Task 9

> Authenticates existing user by email + password, returns JWT tokens.

#### Subtasks

- [ ] **5.1** — Create `LoginUserUseCase.java` in `application/usecase/`
  - Annotate with `@Service` and `@Transactional(readOnly = true)`
  - Constructor-inject: `UserRepository`, `PasswordEncoder`, `JwtService`
  - Method: `AuthTokensDto execute(LoginUserCommand command)`

- [ ] **5.2** — Create `LoginUserCommand.java` record: `String email`, `String password`

- [ ] **5.3** — Implement `execute` logic:
  1. Find user: `userRepository.findByEmail(new Email(command.email()))` → if empty, throw `InvalidCredentialsException` (do NOT reveal "email not found" — always use generic message)
  2. Verify password: `passwordEncoder.matches(command.password(), user.getPasswordHash())` → if false, throw `InvalidCredentialsException`
  3. Generate tokens: `jwtService.generateTokens(user.getId(), user.getEmail())`
  4. Return `AuthTokensDto`

- [ ] **5.4** — Create `InvalidCredentialsException.java` in `domain/exception/`
  - Extends `RuntimeException`
  - Fixed message: `"Invalid email or password"` (never reveal which is wrong)

#### Acceptance Criteria

- [ ] Valid credentials return tokens
- [ ] Wrong password returns `InvalidCredentialsException`
- [ ] Unknown email returns same `InvalidCredentialsException` (no information leak)
- [ ] `@Transactional(readOnly = true)` used (no writes)

---

### Task 6: Refresh token use case

**Priority:** High
**Size:** S
**Depends on:** Task 9

> Validates an existing refresh token and issues new access + refresh tokens.

#### Subtasks

- [ ] **6.1** — Create `RefreshTokenUseCase.java` in `application/usecase/`
  - Annotate `@Service`, `@Transactional(readOnly = true)`
  - Constructor-inject: `JwtService`, `UserRepository`
  - Method: `AuthTokensDto execute(String refreshToken)`

- [ ] **6.2** — Implement `execute` logic:
  1. Call `jwtService.validateRefreshToken(refreshToken)` → extract `UserId`
  2. Load user: `userRepository.findById(userId)` → throw `InvalidCredentialsException` if not found
  3. Generate new tokens: `jwtService.generateTokens(user.getId(), user.getEmail())`
  4. Return `AuthTokensDto`

- [ ] **6.3** — If refresh token rotation is desired (recommended): invalidate the old refresh token
  - Option A (simple): Store refresh tokens in DB with `refresh_tokens` table (Sprint 1 scope: simple, skip rotation for MVP)
  - Option B (MVP): Stateless — just validate and re-issue (use this for Sprint 1)
  - Document the decision as a comment in `RefreshTokenUseCase`

#### Acceptance Criteria

- [ ] Valid refresh token issues new access token
- [ ] Expired or invalid refresh token throws `InvalidCredentialsException` (mapped to 401)
- [ ] Design decision (stateless vs rotation) documented in code comment

---

### Task 7: Auth controller

**Priority:** High
**Size:** M
**Depends on:** Task 4, Task 5, Task 6

> REST endpoints as specified in `API_REGISTRY.md` section 2.1.

#### Subtasks

- [ ] **7.1** — Create `AuthController.java` in `modules/identity/src/main/java/com/connecthealth/identity/presentation/`
  - Annotate with `@RestController`, `@RequestMapping("/api/v1/auth")`
  - Constructor-inject: `RegisterUserUseCase`, `LoginUserUseCase`, `RefreshTokenUseCase`

- [ ] **7.2** — Implement `POST /auth/register` endpoint
  - Request body: `RegisterRequest` record: `@NotBlank String name`, `@Email String email`, `@NotBlank @Size(min=8) String password`
  - Handler annotated: `@PostMapping("/register")`, `@ResponseStatus(HttpStatus.CREATED)`
  - Flow: validate → build `RegisterUserCommand` → call use case → map to `AuthResponse` → return
  - Response body matches API_REGISTRY spec:
    ```json
    { "data": { "user": {...}, "tokens": { "accessToken": "...", "refreshToken": "...", "expiresIn": 900 } } }
    ```

- [ ] **7.3** — Implement `POST /auth/login` endpoint
  - Request body: `LoginRequest` record: `@Email String email`, `@NotBlank String password`
  - Handler: `@PostMapping("/login")`, returns `200 OK`
  - Same flow as register

- [ ] **7.4** — Implement `POST /auth/refresh` endpoint
  - Request body: `RefreshTokenRequest` record: `@NotBlank String refreshToken`
  - Handler: `@PostMapping("/refresh")`, returns `200 OK`

- [ ] **7.5** — Create `GlobalExceptionHandler.java` in `bootstrap/src/main/java/.../presentation/` (or shared location)
  - Annotate with `@RestControllerAdvice`
  - Handle `EmailAlreadyExistsException` → `409 Conflict` with `{"error": {"code": "EMAIL_CONFLICT", "message": "..."}}`
  - Handle `InvalidCredentialsException` → `401 Unauthorized`
  - Handle `MethodArgumentNotValidException` → `400 Bad Request` with field errors
  - Handle generic `Exception` → `500 Internal Server Error`

- [ ] **7.6** — Create response wrapper classes
  - `ApiResponse<T>` record: `T data`, `MetaDto meta` (with `String timestamp`)
  - `ErrorResponse` record: `ErrorDto error` → `ErrorDto`: `String code`, `String message`
  - Use these in all controller responses

#### Acceptance Criteria

- [ ] `POST /api/v1/auth/register` returns 201 with user + tokens
- [ ] `POST /api/v1/auth/login` returns 200 with tokens on valid credentials
- [ ] `POST /api/v1/auth/login` returns 401 on wrong credentials
- [ ] `POST /api/v1/auth/register` returns 409 when email already exists
- [ ] `@Valid` on request bodies triggers validation
- [ ] No domain entities in response — only DTOs

---

### Task 8: Spring Security configuration

**Priority:** High
**Size:** M
**Depends on:** Task 9 (JwtService)

> Configure Spring Security to allow auth endpoints publicly, require JWT for all others.

#### Subtasks

- [ ] **8.1** — Add Spring Security and JWT library dependencies to `modules/identity/build.gradle` and/or `bootstrap/build.gradle`
  - `implementation 'org.springframework.boot:spring-boot-starter-security'`
  - `implementation 'io.jsonwebtoken:jjwt-api:0.12.x'`
  - `runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.x'`
  - `runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.x'`

- [ ] **8.2** — Create `SecurityConfig.java` in `bootstrap/src/main/java/com/connecthealth/bootstrap/config/`
  - Annotate with `@Configuration`, `@EnableWebSecurity`
  - Bean: `SecurityFilterChain`
  - Disable CSRF (`csrf.disable()`)
  - Session: `STATELESS`
  - Permit all: `/api/v1/auth/**`, `/api/v1/health`
  - All other requests: `authenticated()`
  - Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`

- [ ] **8.3** — Create `JwtAuthenticationFilter.java` in `bootstrap/.../config/` or `identity/.../infrastructure/`
  - Extends `OncePerRequestFilter`
  - Logic: extract `Authorization: Bearer <token>` header → validate via `JwtService` → extract user ID → load user → set `SecurityContextHolder`
  - On invalid/expired token: do not throw — just don't set auth context (Spring Security will return 401 downstream)

- [ ] **8.4** — Create `PasswordEncoderConfig.java` bean
  - `@Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }`
  - Place in `bootstrap/config/` or `identity/infrastructure/config/`
  - 12 rounds as per non-functional requirements in PRD

#### Acceptance Criteria

- [ ] `GET /api/v1/health` returns 200 without any token
- [ ] `GET /api/v1/profile` (or any protected route) returns 401 without token
- [ ] Valid JWT in `Authorization` header grants access to protected routes
- [ ] Password encoder uses BCrypt with 12 rounds

---

### Task 9: JWT infrastructure service

**Priority:** High
**Size:** M
**Depends on:** nothing (can start in parallel)

> Centralized JWT generation, validation, and parsing. Used by use cases and the auth filter.

#### Subtasks

- [ ] **9.1** — Add JWT secret and expiry to `application.yml` and `application-dev.yml`
  ```yaml
  app:
    jwt:
      secret: ${JWT_SECRET:dev-secret-key-at-least-256-bits-long-for-hs256}
      access-token-expiry: 900        # 15 minutes in seconds
      refresh-token-expiry: 604800    # 7 days in seconds
  ```
  - Never commit real secrets — use environment variable `${JWT_SECRET}`

- [ ] **9.2** — Create `JwtProperties.java` in `bootstrap/config/` or `identity/infrastructure/`
  - Annotate with `@ConfigurationProperties(prefix = "app.jwt")`
  - Fields: `String secret`, `long accessTokenExpiry`, `long refreshTokenExpiry`

- [ ] **9.3** — Create `JwtService.java` in `identity/infrastructure/security/` or `bootstrap/config/`
  - Mark as `@Service` or `@Component`
  - Method: `generateTokens(UserId userId, Email email): TokenPair`
    - Create access token: signed HS256, subject = `userId.getValue().toString()`, claim `email`, expiry = now + 15min
    - Create refresh token: signed HS256, subject = userId, claim `type: refresh`, expiry = now + 7d
  - Method: `validateAccessToken(String token): Claims` — parse and validate, throw `InvalidTokenException` if expired/invalid
  - Method: `validateRefreshToken(String token): UserId` — same but check `type` claim == `"refresh"`
  - Method: `extractUserId(String token): UserId` — parse subject without validation (for filter use)
  - Use `io.jsonwebtoken.Jwts` builder API (JJWT 0.12.x style)

- [ ] **9.4** — Create `InvalidTokenException.java` in `domain/exception/`
  - Extends `RuntimeException`
  - Maps to `401 Unauthorized` in `GlobalExceptionHandler`

#### Acceptance Criteria

- [ ] Generated access token is a valid JWT decodable at jwt.io
- [ ] Access token expires after 15 minutes (900s)
- [ ] Refresh token expires after 7 days
- [ ] `validateAccessToken` throws on expired token
- [ ] Secret never hardcoded — uses env variable with dev fallback only

---

### Task 10: Tests

**Priority:** High
**Size:** L
**Depends on:** Tasks 1–9

> Achieve ≥80% coverage on domain + application layers. Key scenarios documented below.

#### Subtasks

- [ ] **10.1** — Unit test `Email` value object in `modules/identity/src/test/java/com/connecthealth/identity/domain/valueobject/EmailTest.java`
  - `valid_email_is_accepted()`
  - `blank_email_throws_exception()`
  - `invalid_format_throws_exception()` (e.g., `"notanemail"`, `"@domain.com"`)

- [ ] **10.2** — Unit test `User` entity in `.../domain/entity/UserTest.java`
  - `user_created_with_valid_fields()`
  - `user_creation_fails_with_blank_name()`
  - `user_creation_fails_with_null_email()`

- [ ] **10.3** — Unit test `RegisterUserUseCase` in `.../application/usecase/RegisterUserUseCaseTest.java`
  - Mock `UserRepository`, `PasswordEncoder`, `JwtService`
  - `register_new_user_returns_tokens()`
  - `register_duplicate_email_throws_EmailAlreadyExistsException()`
  - Verify `passwordEncoder.encode()` was called
  - Verify `userRepository.save()` was called with correct entity

- [ ] **10.4** — Unit test `LoginUserUseCase` in `.../LoginUserUseCaseTest.java`
  - `login_valid_credentials_returns_tokens()`
  - `login_wrong_password_throws_InvalidCredentialsException()`
  - `login_unknown_email_throws_InvalidCredentialsException()`
  - Verify same exception type for both failure cases (no information leak)

- [ ] **10.5** — `@WebMvcTest` for `AuthController` in `.../presentation/AuthControllerTest.java`
  - Mock `RegisterUserUseCase`, `LoginUserUseCase`, `RefreshTokenUseCase`
  - Test: `POST /api/v1/auth/register` with valid body → 201
  - Test: `POST /api/v1/auth/register` with blank name → 400
  - Test: `POST /api/v1/auth/login` with valid body → 200
  - Test: `POST /api/v1/auth/login` → 401 when use case throws `InvalidCredentialsException`

- [ ] **10.6** — Integration test `UserRepository` with Testcontainers
  - In `.../infrastructure/persistence/UserRepositoryImplIntTest.java`
  - Annotate with `@SpringBootTest`, `@Testcontainers`
  - Use `@Container PostgreSQLContainer`
  - `save_and_find_by_id_returns_user()`
  - `save_and_find_by_email_returns_user()`
  - `exists_by_email_returns_true_after_save()`

#### Acceptance Criteria

- [ ] `./gradlew test` passes with 0 failures
- [ ] Coverage report shows ≥80% on domain + application layers
- [ ] No tests use `System.out.println` — use SLF4J or assertions only

---

## Dependencies on Other Projects

| This task | Depends on | Project |
|-----------|-----------|---------|
| fit-mobile Task 3 (auth store) | Auth endpoints running locally | fit-api Tasks 7+8 deployed/running |
| fit-mobile Task 4 (login/register screens) | `POST /auth/register` and `POST /auth/login` working | fit-api Tasks 7+8 |

---

## Notes

- **JWT library**: Use JJWT 0.12.x (`io.jsonwebtoken`). Avoid `jjwt` 0.9.x — deprecated API.
- **BCrypt rounds**: 12 as specified in PRD non-functional requirements.
- **Token expiry**: access=15min (900s), refresh=7d (604800s) as specified in PRD.
- **API_REGISTRY endpoint names**: Registry uses `/auth/register` and `/auth/login`. The mobile scaffold uses `/auth/signup` and `/auth/signin`. **The backend takes precedence** — use `/register` and `/login`. Update `auth.api.ts` in fit-mobile after this sprint.
- **Refresh token strategy**: Stateless for MVP (Sprint 1). Rotation can be added in a later sprint using a `refresh_tokens` table.
- **Branch**: `feature/identity` on fit-api.
