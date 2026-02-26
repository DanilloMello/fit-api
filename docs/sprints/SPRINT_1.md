# Sprint 1: Identity — fit-api

> **Generated:** 2026-02-26
> **Sprint:** 1
> **Project:** fit-api
> **Status:** In Progress

---

## Sprint Overview

Sprint 1 implementa o módulo de identidade: entidade User, casos de uso de autenticação (register/login/refresh), geração e validação de JWT, e o endpoint de profile. É a fundação de segurança que todos os sprints seguintes dependem.

---

## Progress

| Task | Status | Priority |
|------|--------|----------|
| User entity e migration | ⬜ Not started | High |
| Spring Security + JWT setup | ⬜ Not started | High |
| Auth endpoints (register/login/refresh) | ⬜ Not started | High |
| Profile endpoints | ⬜ Not started | Medium |
| Testes de autenticação | ⬜ Not started | Medium |

---

## Tasks

### Task 1: User Entity e Migration

**Priority:** High
**Size:** S
**Depends on:** nothing

> Criar a entidade JPA User e a migration Flyway V1 que define a tabela `users` no banco.

#### Subtasks

- [ ] **1.1** — Criar `User.java` em `modules/identity/src/main/java/com/connecthealth/identity/model/`
  - Campos: `id` (UUID, `@GeneratedValue(strategy = GenerationType.UUID)`), `name` (String, not null), `email` (String, unique, not null), `password` (String, not null — hash bcrypt), `phone` (String, nullable), `photoUrl` (String, nullable), `createdAt` (Instant, not null, `@Column(updatable = false)`)
  - Anotações: `@Entity`, `@Table(name = "users")`
  - Construtor protegido sem args + construtor com campos obrigatórios
  - Getters e setters para todos os campos

- [ ] **1.2** — Criar migration `V1__identity_users.sql` em `bootstrap/src/main/resources/db/migration/`
  ```sql
  CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    photo_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
  );
  ```

- [ ] **1.3** — Criar `UserRepository.java` em `modules/identity/src/main/java/com/connecthealth/identity/repository/`
  - Extends `JpaRepository<User, UUID>`
  - Método: `Optional<User> findByEmail(String email)`
  - Método: `boolean existsByEmail(String email)`

- [ ] **1.4** — Validar que `./gradlew bootRun` aplica a migration sem erros

#### Acceptance Criteria

- [ ] Tabela `users` criada no PostgreSQL após `bootRun`
- [ ] `UserRepository` pode salvar e buscar um `User`
- [ ] Flyway registra `V1` como applied

---

### Task 2: Spring Security + JWT Setup

**Priority:** High
**Size:** M
**Depends on:** Task 1

> Configurar Spring Security com autenticação stateless via JWT. Definir filtro de autenticação, `UserDetails`, e utilitário de geração/validação de tokens.

#### Subtasks

- [ ] **2.1** — Adicionar dependências no `modules/identity/build.gradle`:
  - `spring-boot-starter-security`
  - `io.jsonwebtoken:jjwt-api:0.12.6`
  - `io.jsonwebtoken:jjwt-impl:0.12.6` (runtime)
  - `io.jsonwebtoken:jjwt-jackson:0.12.6` (runtime)
  - Verificar versão mais recente via MCP context7 antes de adicionar

- [ ] **2.2** — Criar `JwtProperties.java` em `modules/identity/src/main/java/com/connecthealth/identity/config/`
  - `@ConfigurationProperties(prefix = "app.jwt")`
  - Campos: `secret` (String), `expirationMs` (long, default 900000 = 15min), `refreshExpirationMs` (long, default 604800000 = 7 dias)
  - Adicionar `app.jwt.secret` e `app.jwt.expiration-ms` em `application.yml`

- [ ] **2.3** — Criar `JwtService.java` em `modules/identity/src/main/java/com/connecthealth/identity/service/`
  - Método: `String generateAccessToken(User user)`
  - Método: `String generateRefreshToken(User user)`
  - Método: `String extractEmail(String token)`
  - Método: `boolean isTokenValid(String token, UserDetails userDetails)`
  - Claims: `sub` = email, `userId` = id UUID

- [ ] **2.4** — Criar `UserPrincipal.java` em `modules/identity/src/main/java/com/connecthealth/identity/security/`
  - Implements `UserDetails`
  - Construtor: `UserPrincipal(User user)`
  - `getId()` retorna `UUID` do User
  - `getAuthorities()` retorna `[ROLE_USER]`
  - `getUsername()` retorna email

- [ ] **2.5** — Criar `UserDetailsServiceImpl.java` em `modules/identity/src/main/java/com/connecthealth/identity/security/`
  - Implements `UserDetailsService`
  - `loadUserByUsername(email)` busca via `UserRepository.findByEmail` → retorna `UserPrincipal`
  - Lança `UsernameNotFoundException` se não encontrar

- [ ] **2.6** — Criar `JwtAuthenticationFilter.java` em `modules/identity/src/main/java/com/connecthealth/identity/security/`
  - Extends `OncePerRequestFilter`
  - Extrai token do header `Authorization: Bearer <token>`
  - Valida via `JwtService` → seta `SecurityContextHolder`

- [ ] **2.7** — Criar `SecurityConfig.java` em `modules/identity/src/main/java/com/connecthealth/identity/config/`
  - `@Configuration`, `@EnableWebSecurity`
  - `SecurityFilterChain` com:
    - `csrf().disable()`
    - `sessionManagement(STATELESS)`
    - `permitAll()` para: `POST /api/v1/auth/**`, `GET /api/v1/health`
    - `authenticated()` para todo o resto
    - `addFilterBefore(JwtAuthenticationFilter, UsernamePasswordAuthenticationFilter)`
  - Bean `PasswordEncoder` → `BCryptPasswordEncoder`
  - Bean `AuthenticationManager`

#### Acceptance Criteria

- [ ] `./gradlew build` compila sem erros com as novas dependências
- [ ] `GET /api/v1/clients` sem token retorna 401
- [ ] `GET /api/v1/health` sem token retorna 200
- [ ] Token JWT gerado pode ser decodificado com o secret configurado

---

### Task 3: Auth Endpoints (Register / Login / Refresh)

**Priority:** High
**Size:** M
**Depends on:** Task 1, Task 2

> Implementar os endpoints `POST /auth/register`, `POST /auth/login` e `POST /auth/refresh` conforme especificado no API_REGISTRY.md.

#### Subtasks

- [ ] **3.1** — Criar DTOs em `modules/identity/src/main/java/com/connecthealth/identity/dto/`:
  - `RegisterRequest.java` — record com: `@NotBlank String name`, `@Email @NotBlank String email`, `@NotBlank @Size(min=8) String password`
  - `LoginRequest.java` — record com: `@Email @NotBlank String email`, `@NotBlank String password`
  - `AuthResponse.java` — record com: `UserResponse user`, `TokensResponse tokens`
  - `UserResponse.java` — record com: `UUID id`, `String name`, `String email`
  - `TokensResponse.java` — record com: `String accessToken`, `String refreshToken`, `long expiresIn`
  - `RefreshRequest.java` — record com: `@NotBlank String refreshToken`

- [ ] **3.2** — Criar `AuthService.java` em `modules/identity/src/main/java/com/connecthealth/identity/service/`
  - `@Service`, `@Transactional`
  - Método `AuthResponse register(RegisterRequest req)`:
    - Verifica se email já existe → lança exceção com mensagem "Email already in use"
    - Encripta senha com `BCryptPasswordEncoder`
    - Salva `User` via `UserRepository`
    - Gera access + refresh tokens via `JwtService`
    - Retorna `AuthResponse`
  - Método `@Transactional(readOnly = true) AuthResponse login(LoginRequest req)`:
    - Busca User por email → lança 401 se não encontrar
    - Verifica senha via `passwordEncoder.matches()` → lança 401 se inválida
    - Gera tokens e retorna `AuthResponse`
  - Método `@Transactional(readOnly = true) AuthResponse refresh(String refreshToken)`:
    - Valida token → extrai email → busca User
    - Gera novo access token
    - Retorna `AuthResponse` com novo access token

- [ ] **3.3** — Criar `AuthController.java` em `modules/identity/src/main/java/com/connecthealth/identity/controller/`
  - `@RestController`, `@RequestMapping("/api/v1/auth")`
  - `POST /register` → 201 com `ApiResponse<AuthResponse>`
  - `POST /login` → 200 com `ApiResponse<AuthResponse>`
  - `POST /refresh` → 200 com `ApiResponse<AuthResponse>`
  - Usa `@Valid` nos request bodies

- [ ] **3.4** — Criar `GlobalExceptionHandler.java` em `modules/identity/src/main/java/com/connecthealth/identity/config/` (ou em `bootstrap/`)
  - `@RestControllerAdvice`
  - Handler para `MethodArgumentNotValidException` → 400 com campo `error.code = "VALIDATION_ERROR"`
  - Handler para email duplicado → 409
  - Handler para credenciais inválidas → 401 com `error.code = "UNAUTHORIZED"`
  - Retorna estrutura: `{ "error": { "code": "...", "message": "...", "details": {} } }`

- [ ] **3.5** — Testar todos os endpoints manualmente:
  - `POST /api/v1/auth/register` com nome, email, senha válidos → 201
  - `POST /api/v1/auth/login` com credenciais corretas → 200 com tokens
  - `POST /api/v1/auth/login` com senha errada → 401
  - `POST /api/v1/auth/register` com email duplicado → 409

#### Acceptance Criteria

- [ ] `POST /auth/register` retorna 201 com `data.user` e `data.tokens`
- [ ] `POST /auth/login` retorna 200 com tokens válidos
- [ ] `POST /auth/login` com credenciais inválidas retorna 401
- [ ] Token retornado em login funciona como Bearer em rotas protegidas
- [ ] `POST /auth/register` com email duplicado retorna erro descritivo

---

### Task 4: Profile Endpoints

**Priority:** Medium
**Size:** S
**Depends on:** Task 2, Task 3

> Implementar `GET /profile` e `PATCH /profile` para que o usuário autenticado consulte e atualize seus dados.

#### Subtasks

- [ ] **4.1** — Criar DTOs em `modules/identity/src/main/java/com/connecthealth/identity/dto/`:
  - `ProfileResponse.java` — record com: `UUID id`, `String name`, `String email`, `String phone`, `String photoUrl`, `Instant createdAt`
    - Método estático `from(User user)`
  - `UpdateProfileRequest.java` — record com: `@NotBlank String name`, `String phone`

- [ ] **4.2** — Criar `ProfileService.java` (ou adicionar métodos em `AuthService`) em `modules/identity/src/main/java/com/connecthealth/identity/service/`
  - `@Transactional(readOnly = true) ProfileResponse getProfile(UUID userId)` — busca User por id
  - `ProfileResponse updateProfile(UUID userId, UpdateProfileRequest req)` — atualiza name e phone

- [ ] **4.3** — Criar `ProfileController.java` em `modules/identity/src/main/java/com/connecthealth/identity/controller/`
  - `@RestController`, `@RequestMapping("/api/v1/profile")`
  - `GET /profile` → 200 com `ApiResponse<ProfileResponse>` (usa `@AuthenticationPrincipal UserPrincipal`)
  - `PATCH /profile` → 200 com `ApiResponse<ProfileResponse>`

#### Acceptance Criteria

- [ ] `GET /api/v1/profile` com token válido retorna dados do usuário logado
- [ ] `PATCH /api/v1/profile` atualiza nome e telefone
- [ ] Sem token → 401

---

### Task 5: Testes de Autenticação

**Priority:** Medium
**Size:** M
**Depends on:** Task 1, Task 2, Task 3

> Cobrir os fluxos críticos de autenticação com testes unitários e de controller.

#### Subtasks

- [ ] **5.1** — Criar `AuthServiceTest.java` em `modules/identity/src/test/java/com/connecthealth/identity/service/`
  - Cenários:
    - `register_success` — novo usuário criado, senha encriptada, tokens retornados
    - `register_duplicateEmail` — lança exceção com mensagem correta
    - `login_success` — credenciais corretas retornam tokens
    - `login_wrongPassword` — lança 401
    - `login_emailNotFound` — lança 401
  - Use `@ExtendWith(MockitoExtension.class)`, mock `UserRepository` e `JwtService`

- [ ] **5.2** — Criar `JwtServiceTest.java` em `modules/identity/src/test/java/com/connecthealth/identity/service/`
  - Cenários:
    - `generateToken_and_extractEmail` — token gerado contém email correto
    - `isTokenValid_withValidToken` — retorna true
    - `isTokenValid_withExpiredToken` — retorna false

- [ ] **5.3** — Criar `AuthControllerTest.java` em `modules/identity/src/test/java/com/connecthealth/identity/controller/`
  - Use `@WebMvcTest(AuthController.class)` com `AuthService` mockado
  - Cenários:
    - `POST /auth/register` — 201 com body correto
    - `POST /auth/register` com body inválido — 400 com `VALIDATION_ERROR`
    - `POST /auth/login` — 200 com tokens
    - `POST /auth/login` com senha errada — 401

- [ ] **5.4** — Verificar que `./gradlew test` passa com 80%+ de cobertura no módulo identity

#### Acceptance Criteria

- [ ] Todos os testes passam com `./gradlew test`
- [ ] Cobertura ≥ 80% em `AuthService` e `JwtService`
- [ ] Controller tests cobrem respostas de sucesso e erro

---

## Dependencies on Other Projects

| Esta task | Depende de | Projeto |
|-----------|-----------|---------|
| Task 3 (Auth endpoints) | fit-mobile já tem `authApi` pronto aguardando os endpoints | fit-mobile |
| Task 4 (Profile) | fit-mobile pode integrar imediatamente após Task 3 | fit-mobile |

---

## Notes

- fit-mobile **já implementou** a tela de login, register e auth store — está aguardando o backend da Sprint 1.
- Ao adicionar dependência JWT, usar `context7` MCP para checar versão mais recente do `jjwt`.
- O `GlobalExceptionHandler` pode ser colocado no módulo `bootstrap` para ser compartilhado entre todos os módulos futuros, ou em `identity` se preferir mantê-lo isolado por ora.
- O endpoint `POST /auth/refresh` por ora pode aceitar o refreshToken no body e emitir um novo accessToken — persistência de refresh token em banco pode ser feita em sprint futura.
- **Após implementar Task 3, atualizar `fit-common/docs/API_REGISTRY.md`** com os payloads completos de register/login/refresh.
