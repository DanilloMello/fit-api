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

The `.mcp.json` at the project root is already configured. When you open this project in Claude Code, the `context7` MCP server connects automatically — no manual setup needed.

Available MCP servers:
- `context7` — live library documentation for dependency version checks

## Working with Shared Documentation

Shared docs live in the **fit-common** GitHub repo. Read them via the GitHub API:

```bash
gh api repos/DanilloMello/fit-common/contents/docs/common/API_REGISTRY.md --jq '.content' | base64 -d
```

To edit shared docs, open the fit-common repo and commit directly:

```bash
git clone https://github.com/DanilloMello/fit-common.git  # once
cd fit-common
# Edit docs/
git add docs/API_REGISTRY.md
git commit -m "docs: update API registry"
git push
```

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
Verify your GitHub CLI is authenticated and can reach fit-common:
```bash
gh api repos/DanilloMello/fit-common/contents/docs/common --jq '.[].name'
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
- Read `.claude/skills/fit-api-overview/SKILL.md` for Java/Spring patterns
