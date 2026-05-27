
# Local Development Runbook

## Requirements

- Java 21
- Maven
- Docker
- Node.js 22+
- npm
- OpenAI API key

## Environment

```bash
export OPENAI_API_KEY=your_openai_api_key_here
export JWT_SECRET=change-me-change-me-change-me-change-me
```

Start Infrastructure

```Bash
docker compose up -d
```

Check containers:

```bash
docker ps
```

Start Services

Recommended order:

- auth-service
- profile-service
- job-service
- matching-service
- document-service
- ai-generation-service
- workflow-service
- api-gateway-service
- frontend

Health Checks
```bash
curl http://localhost:8079/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
curl http://localhost:8086/actuator/health
```

Login
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
-H "Content-Type: application/json" \
-d '{"username":"demo","password":"demo"}' | jq -r '.accessToken')
```

Troubleshooting
CORS error

Check gateway CORS config and make sure OPTIONS is permitted:
```java
.pathMatchers(HttpMethod.OPTIONS).permitAll()
```

Kafka messages not consumed

Check topic:

```bash
docker exec -it careerflow-kafka /opt/kafka/bin/kafka-topics.sh \
--bootstrap-server localhost:9092 \
--list
```

MinIO document missing

Open:

http://localhost:9001

Check bucket:

careerflow-documents
OpenAI key not picked up

Run service with:

```text
OPENAI_API_KEY=$OPENAI_API_KEY mvn spring-boot:run
```