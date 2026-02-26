# Sprint 0: Foundation — fit-api

> **Generated:** 2026-02-26
> **Sprint:** 0
> **Project:** fit-api
> **Status:** Completed

---

## Sprint Overview

Sprint 0 estabelece a fundação técnica do backend: estrutura Gradle multi-module, ambiente de banco de dados via Docker Compose e o endpoint de health check. Todos os itens foram implementados.

---

## Progress

| Task | Status | Priority |
|------|--------|----------|
| Gradle multi-module setup | ✅ Done | High |
| Docker Compose (PostgreSQL) | ✅ Done | High |
| Health endpoint | ✅ Done | High |

---

## Tasks

### Task 1: Gradle Multi-Module Setup

**Priority:** High
**Size:** M
**Depends on:** nothing

> Estruturar o projeto como Gradle multi-module com módulos shared, identity, client, training e bootstrap.

#### Subtasks

- [x] **1.1** — Criar `settings.gradle` com `include` para todos os módulos
  - Módulos: `bootstrap`, `modules:shared`, `modules:identity`, `modules:client`, `modules:training`
- [x] **1.2** — Configurar `build.gradle` raiz com plugins compartilhados (Java 21, Spring Boot 3.2)
- [x] **1.3** — Criar `build.gradle` por módulo com dependências específicas
- [x] **1.4** — Configurar `modules/shared` como dependência dos demais módulos
- [x] **1.5** — Validar build com `./gradlew build`

#### Acceptance Criteria

- [x] `./gradlew build` compila todos os módulos sem erros
- [x] Módulos isolados com packages `com.connecthealth.{module}`
- [x] Módulo `shared` acessível por todos os outros

---

### Task 2: Docker Compose (PostgreSQL 15)

**Priority:** High
**Size:** S
**Depends on:** Task 1

> Configurar `docker-compose.yml` para subir PostgreSQL 15 com as credenciais corretas.

#### Subtasks

- [x] **2.1** — Criar `docker-compose.yml` na raiz com serviço `postgres` (image: postgres:15)
  - Credenciais: `connecthealth / connecthealth`, db: `connecthealth`
  - Porta: `5432:5432`
- [x] **2.2** — Configurar `application.yml` com datasource apontando para `localhost:5432/connecthealth`
- [x] **2.3** — Configurar Flyway com `baseline-on-migrate: true`

#### Acceptance Criteria

- [x] `docker compose up -d` sobe o PostgreSQL sem erros
- [x] Aplicação conecta ao banco via `./gradlew bootRun`
- [x] Flyway inicializa sem erros (mesmo sem migrations)

---

### Task 3: Health Endpoint

**Priority:** High
**Size:** S
**Depends on:** Task 1, Task 2

> Implementar `GET /api/v1/health` que retorna status UP para monitoramento básico.

#### Subtasks

- [x] **3.1** — Criar `HealthController.java` em `bootstrap/src/main/java/com/connecthealth/bootstrap/presentation/`
  - Endpoint: `GET /api/v1/health`
  - Retorna: `{ "status": "UP", "timestamp": "..." }`
- [x] **3.2** — Criar `ApiResponse.java` em `modules/shared/src/main/java/com/connecthealth/shared/dto/`
  - Wrapper genérico `ApiResponse<T>` com campos `data` e `meta`
- [x] **3.3** — Testar endpoint manualmente com `curl http://localhost:8080/api/v1/health`

#### Acceptance Criteria

- [x] `GET /api/v1/health` retorna HTTP 200 com `{ "status": "UP" }`
- [x] `ApiResponse<T>` disponível no módulo shared para uso futuro

---

## Dependencies on Other Projects

Nenhuma dependência em fit-mobile para esta sprint.

---

## Notes

- Sprint 0 foi concluída com sucesso. O `HealthController` retorna `Map` diretamente em vez de usar `ApiResponse` — não é um blocker, pode ser refatorado futuramente.
- O módulo `shared` passou por refatoração de Clean Architecture → MVC (commit `c9259f5`), removendo domain layer desnecessária.
