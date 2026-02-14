# ConnectHealth fit-api Quick Start

## Prerequisites

- Git 2.13+
- Java 21 (JDK)
- Docker & Docker Compose
- Node.js 18+ (for MCP servers)

## Clone Repositories

Both repos must be siblings under the same parent directory:

```bash
git clone https://github.com/DanilloMello/fit-api.git
git clone https://github.com/DanilloMello/fit-common.git
```

Expected layout:

```
projetos/
├── fit-api/
├── fit-common/
└── fit-mobile/     # (optional, for frontend work)
```

## Install Pre-Push Hook

```bash
cd fit-common
./scripts/install-hooks.sh
```

This installs the **pre-push hook** that validates code quality before pushing (tests, build, format, guidelines).

## Start Development

```bash
cd fit-api

# Start PostgreSQL
docker compose up -d

# Run the app
./gradlew bootRun

# Run tests
./gradlew test
```

## MCP Setup

The `.claude/mcp.json` is already configured. When you open this project in Claude Code, it automatically connects to fit-common docs via MCP servers. No manual setup needed.

Available MCP servers:
- `fit-api-docs` — shared docs (DOMAIN_SPEC, API_REGISTRY, etc.)
- `fit-api-skills` — Java/Spring patterns
- `fit-api-scripts` — automation scripts
- `fit-api-hooks` — git hook templates

## Working with Shared Documentation

Shared docs live in the **fit-common** repo. To edit:

```bash
cd ../fit-common
# Edit files in docs/
git add docs/API_REGISTRY.md
git commit -m "docs: update API registry"
git push
```

No submodule sync needed — MCP reads directly from fit-common.

## Daily Workflow

```bash
git pull                    # Pull latest changes
# ... make changes ...
git commit -m "feat: ..."   # Commit
git push                    # Pre-push hook validates automatically
```

## Troubleshooting

### Pre-push hook fails
Fix the errors shown by the hook, then push again.

### MCP not connecting
Verify fit-common is cloned as a sibling directory:
```bash
ls ../fit-common/docs/
# Should list: API_REGISTRY.md, DOMAIN_SPEC.md, etc.
```

### Build fails with Java 21 error
Ensure you have Java 21 installed:
```bash
java -version  # Should show Java 21
```

### Docker issues
```bash
docker compose down
docker compose up -d
```

## Next Steps

- Read `CLAUDE.md` for project overview
- Use `fit-api-docs` MCP to read `DOMAIN_SPEC.md` for the domain model
- Use `fit-api-docs` MCP to read `API_REGISTRY.md` for API endpoints
- Read `.claude/skills/fit-api/SKILL.md` for Java/Spring patterns
