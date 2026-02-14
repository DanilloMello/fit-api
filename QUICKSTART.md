# ConnectHealth fit-api Quick Start

## Prerequisites

- Git 2.13+ (for submodule support)
- Java 21
- Docker (for PostgreSQL)

## Clone Repository

```bash
# Clone with submodules
git clone --recurse-submodules https://github.com/DanilloMello/fit-api.git
cd fit-api

# Ensure fit-common is on latest version
git submodule update --remote .claude/common

# If you already cloned without --recurse-submodules:
git submodule init
git submodule update --remote .claude/common
```

## Verify Setup

```bash
# Check submodule status
git submodule status
# Should show: [hash] .claude/common (heads/master)

# Check documentation is available
ls .claude/common/docs/
# Should show: API_REGISTRY.md, DOMAIN_SPEC.md, etc.
```

## Install Git Hooks

```bash
# Install pre-push validation hooks + automated submodule updates
./.claude/common/scripts/install-hooks.sh
```

This installs:
- **pre-commit hook** - Warns if fit-common documentation is outdated
- **post-merge hook** - Auto-updates fit-common after `git pull`
- **post-checkout hook** - Auto-updates fit-common when switching branches
- **pre-push hook** - Validates code quality before push (tests, build, etc.)
- **git config** - Sets `submodule.recurse = true` for automatic updates

## Run Application

```bash
# Start PostgreSQL
docker compose up -d

# Run application
./gradlew bootRun

# Run tests
./gradlew test
```

## Working with Shared Documentation

### Read Documentation

All shared docs are in `.claude/common/docs/`:
- `DOMAIN_SPEC.md` - Entities and business rules
- `API_REGISTRY.md` - API endpoints
- `CODING_GUIDELINES.md` - Code standards
- `VALIDATION_SETUP.md` - Hook and CI/CD setup
- `SUBMODULE_GUIDE.md` - Git submodule workflow

### Update Documentation

```bash
# Navigate to submodule
cd .claude/common

# Edit files
vim docs/API_REGISTRY.md

# Commit to fit-common
git add docs/API_REGISTRY.md
git commit -m "docs: add endpoint for X"
git push origin master

# Return to fit-api
cd ../..

# Update submodule reference
git add .claude/common
git commit -m "chore: update fit-common"
git push
```

### Pull Latest Documentation

```bash
# Automatic (with git config and post-merge hook):
git pull

# Manual:
git submodule update --remote .claude/common
git add .claude/common
git commit -m "chore: update fit-common to latest"
git push
```

## Daily Workflow

```bash
# 1. Pull latest changes (including submodules)
git pull
# post-merge hook automatically updates .claude/common ✅

# 2. Create feature branch
git checkout -b feature/my-feature

# 3. Make changes...
# ...

# 4. Commit
git commit -m "feat: add my feature"
# pre-commit hook checks if .claude/common is outdated ✅

# 5. Push
git push origin feature/my-feature
# pre-push hook validates code quality ✅

# 6. Create PR on GitHub
```

## Automated Workflow

Once hooks are installed:

### On `git pull`:
- **post-merge hook** auto-updates `.claude/common/`
- Documentation always fresh ✅

### On `git checkout`:
- **post-checkout hook** updates `.claude/common/` when switching branches
- Documentation matches your branch context ✅

### On `git commit`:
- **pre-commit hook** warns if `.claude/common/` is behind remote
- Option to cancel and update first ✅

### On `git push`:
- **pre-push hook** validates:
  - Code format (Spotless)
  - Build (Gradle)
  - Tests
  - API Registry sync
  - Coding guidelines
  - SonarLint (if configured)

## Troubleshooting

### Submodule not found

```bash
git submodule init
git submodule update --remote .claude/common
```

### fit-common is outdated

```bash
git submodule update --remote .claude/common
git add .claude/common
git commit -m "chore: update fit-common"
```

### Build fails with Java 21 error

Ensure you have Java 21 installed:
```bash
java -version  # Should show Java 21
```

### Pre-push hook fails

Fix the errors shown by the hook, then push again. To bypass (NOT recommended):
```bash
git push --no-verify
```

See `.claude/common/docs/SUBMODULE_GUIDE.md` for detailed troubleshooting.

## Next Steps

- Read `CLAUDE.md` for project overview
- Read `.claude/common/docs/DOMAIN_SPEC.md` for domain model
- Read `.claude/common/docs/CODING_GUIDELINES.md` for code standards
- Read `.claude/common/docs/API_REGISTRY.md` for API endpoints

## Resources

- [Git Submodules Guide](https://git-scm.com/book/en/v2/Git-Tools-Submodules)
- [fit-common SUBMODULE_GUIDE.md](./.claude/common/docs/SUBMODULE_GUIDE.md)
- [VALIDATION_SETUP.md](./.claude/common/docs/VALIDATION_SETUP.md)
