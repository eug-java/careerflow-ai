
# Local Development Runbook

## Requirements

- Java 21
- Maven 3.9+
- Docker (required for Testcontainers integration tests and local infra)
- Node.js 22+
- npm
- OpenAI API key (for AI generation)

## Quick Start

```bash
cp .env.example .env
make infra-up
make backend-verify
cd frontend/web-app && npm ci && npm run dev
```

Full stack with all services in Docker:

```bash
docker compose up -d --build
# or
make up
```

## Environment

```bash
export OPENAI_API_KEY=your_openai_api_key_here
export JWT_SECRET=change-me-change-me-change-me-change-me
export CAREERFLOW_INTERNAL_API_KEY=local-internal-key
export CAREERFLOW_ADMIN_PASSWORD=ChangeMeNow123!
```

## Infrastructure Ports

| Service | Port |
|---------|------|
| profile-postgres | 5432 |
| job-postgres | 5433 |
| matching-postgres | 5434 |
| document-postgres | 5435 |
| workflow-postgres | 5436 |
| auth-postgres | 5437 |
| Kafka | 9092 |
| MinIO API / Console | 9000 / 9001 |
| Zeebe | 26500 |
| Prometheus | 9090 |
| Grafana | 3001 |

## Start Services (local JVM)

Recommended order:

1. auth-service (8079) — requires auth-postgres
2. profile-service (8081)
3. job-service (8082)
4. matching-service (8083)
5. document-service (8085)
6. ai-generation-service (8084)
7. workflow-service (8086)
8. api-gateway-service (8080)
9. frontend (5173)

## Testing

```bash
make backend-verify          # unit + integration + JaCoCo
make frontend-test           # Vitest
cd frontend/web-app && npm run test:e2e   # Playwright (optional)
make docker-build            # verify Dockerfiles
```

Integration smoke test: `backend/document-service/.../DocumentPipelineSmokeIT` (Testcontainers: Postgres + MinIO).

## Health Checks

```bash
curl http://localhost:8079/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
curl http://localhost:8086/actuator/health
```

## Login

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo"}' | jq -r '.accessToken')
```

Refresh token:

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"YOUR_REFRESH_TOKEN"}'
```

## WebSocket Workflow Status

Connect with JWT (owner must match workflow):

```text
ws://localhost:8080/ws/workflows/status?processInstanceKey=123&token=ACCESS_TOKEN
```

## Troubleshooting

### CORS error

Check gateway CORS config and ensure OPTIONS is permitted.

### Kafka messages not consumed

```bash
docker exec -it careerflow-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list
```

### MinIO document missing

Open http://localhost:9001 and check bucket `careerflow-documents`.

### OpenAI key not picked up

```bash
OPENAI_API_KEY=$OPENAI_API_KEY mvn spring-boot:run
```

### Correlation ID tracing

Pass `X-Correlation-Id` header through the gateway; it is forwarded to downstream services and returned in the response.
