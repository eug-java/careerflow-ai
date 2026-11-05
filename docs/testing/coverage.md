# Test coverage

## Backend (JaCoCo)

CI and local quality gate:

```bash
make backend-verify
# or
mvn -B -ntp -f backend/pom.xml clean verify
```

| Setting | Value |
|---------|--------|
| Tool | JaCoCo 0.8.15 (`backend/pom.xml`) |
| Gate | **≥ 50% line coverage** per module bundle |
| Excluded from gate | `*Application` main classes (all modules) |
| Extra exclusions | `workflow-service`: Zeebe workers, HTTP clients, `WorkflowController`, WebSocket config, selected DTOs |

HTML reports (after `verify`):

```text
backend/<module>/target/site/jacoco/index.html
```

CI uploads `backend/**/target/site/jacoco/` as the `jacoco-reports` artifact.

### Unit vs integration

| Kind | Location | Runs in |
|------|----------|---------|
| Unit / slice tests | `*Test.java` (Surefire) | `mvn test` / `verify` |
| Integration | `*IT.java` (Failsafe) | `mvn verify` only |

Current integration tests:

- `auth-service`: `AuthFlowIT` — register, login, refresh rotation, logout (Testcontainers Postgres)
- `document-service`: `DocumentPipelineSmokeIT` — Kafka event → Postgres + MinIO (Testcontainers)

### Test class counts (unit `*Test.java`)

| Module | Test classes |
|--------|----------------|
| common-lib | 9 |
| api-gateway-service | 6 |
| profile-service | 5 |
| job-service | 4 |
| matching-service | 8 |
| ai-generation-service | 7 |
| document-service | 7 |
| workflow-service | 7 |
| auth-service | 8 |
| email-service | 8 |

**Portfolio note:** `workflow-service` keeps Camunda workers and Zeebe wiring mostly integration-tested manually or via full Docker stack; JaCoCo excludes reflect that.

## Frontend (Vitest)

```bash
make frontend-test
# or
cd frontend/web-app && npm test
```

| Setting | Value |
|---------|--------|
| Runner | Vitest 5 |
| Current unit tests | 4 files, 9 tests (`src/**/*.test.ts`) |
| Coverage gate | **None** (no `@vitest/coverage-v8` threshold in CI) |

E2E (Playwright) — not counted in unit coverage:

```bash
cd frontend/web-app && npm run test:e2e          # UI specs
cd frontend/web-app && npm run test:e2e:fullstack # @fullstack (needs compose.e2e.yaml or full stack)
```

## Suggested improvements (optional)

- Add Vitest coverage report in CI (informational, no hard gate).
- Add slice tests for untested controllers only if coverage drops below 50% on a module.
- Prefer integration tests over widening JaCoCo excludes when adding Zeebe/Kafka paths.
