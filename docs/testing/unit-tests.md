# Unit tests

See also: [coverage.md](./coverage.md) for JaCoCo gates, integration tests, and frontend Vitest.

## Backend

### Run all unit tests (no integration)

```bash
mvn -B -ntp -f backend/pom.xml test
```

### Run full verify (unit + integration + JaCoCo check)

```bash
make backend-verify
```

### Run one module

```bash
mvn -B -ntp -f backend/pom.xml test -pl :profile-service -am
```

### Run one test class

```bash
mvn -B -ntp -f backend/pom.xml test -pl :profile-service -Dtest=CandidateProfileServiceTest
```

### Recommended test dependencies

Each backend service includes:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Frontend

```bash
cd frontend/web-app && npm ci && npm test
```

Watch mode: `npm run test:watch`.
