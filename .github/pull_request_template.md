## Description

<!-- Provide a brief description of the changes in this PR -->

## Type of Change

- [ ] 🐛 Bug fix (non-breaking change which fixes an issue)
- [ ] ✨ New feature (non-breaking change which adds functionality)
- [ ] 💥 Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] 🔧 Configuration change
- [ ] 📝 Documentation update
- [ ] ♻️ Code refactoring (no functional changes)
- [ ] 🧪 Test update

## Related Issues

<!-- Link to related issues or tickets -->
Closes #

## Changes Made

<!-- List the main changes made in this PR -->

-
-
-

## Pre-Push Validation

- [ ] ✅ All tests pass (`./gradlew test`)
- [ ] ✅ Build succeeds (`./gradlew build`)
- [ ] ✅ Code is formatted (Google Java Format / Spotless)
- [ ] ✅ No System.out.println statements
- [ ] ✅ No @Transactional on repositories (only on Use Cases)
- [ ] ✅ SonarLint shows no critical/blocker issues
- [ ] ✅ Pre-push hook passes

## API Changes

- [ ] 📝 Updated `API_REGISTRY.md` if endpoints were added/modified
- [ ] 🔗 API changes are backwards compatible OR breaking changes are documented
- [ ] 📋 New endpoints follow RESTful conventions

## Domain Changes

- [ ] 📝 Updated `DOMAIN_SPEC.md` if entities/enums were added/modified
- [ ] 🏗️ Follows DDD architecture (domain/application/infrastructure/presentation)
- [ ] 🧪 Domain logic has unit tests

## Database Changes

- [ ] 📝 Updated `DATABASE.md` if schema changes were made
- [ ] 🔄 Flyway migration scripts added (if applicable)
- [ ] ⚡ Indexes added for new queries (if applicable)

## Testing

<!-- Describe the testing performed -->

- [ ] 🧪 Unit tests added/updated
- [ ] 🔗 Integration tests added/updated (if applicable)
- [ ] ✅ Test coverage maintained or improved
- [ ] 🎯 Edge cases covered

## Code Quality Checklist

- [ ] 📦 Code follows DDD layered architecture
- [ ] 🎯 Single Responsibility Principle followed
- [ ] 🔒 Input validation at boundaries (controllers, domain)
- [ ] 🏷️ Proper exception handling
- [ ] 📝 Complex logic is documented
- [ ] ♻️ No code duplication
- [ ] 🚫 No commented-out code

## Deployment Notes

<!-- Any special deployment considerations? -->

- [ ] Requires database migration
- [ ] Requires environment variable changes
- [ ] Requires configuration updates
- [ ] No special deployment steps needed

## Screenshots (if applicable)

<!-- Add screenshots for UI changes -->

## Additional Context

<!-- Add any other context about the PR here -->

---

## Reviewer Checklist

- [ ] Code follows established patterns and guidelines
- [ ] Tests are adequate and passing
- [ ] API_REGISTRY.md updated if endpoints changed
- [ ] DOMAIN_SPEC.md updated if entities/enums changed
- [ ] No security vulnerabilities introduced
- [ ] Performance impact considered
- [ ] Error handling is appropriate
- [ ] Documentation is sufficient

---

**🤖 Generated with [Claude Code](https://claude.com/claude-code)**
