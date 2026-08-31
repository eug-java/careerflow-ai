JAVA_HOME ?= /usr/lib/jvm/java-21-openjdk-amd64
export JAVA_HOME

.PHONY: help up down infra-up infra-down backend-verify frontend-install frontend-test frontend-build docker-build compose-validate

help:
	@echo "CareerFlow AI developer commands"
	@echo "  make up                Build and start the FULL stack (compose.yaml)"
	@echo "  make down              Stop the full stack"
	@echo "  make infra-up          Start infrastructure only (docker-compose.yml)"
	@echo "  make infra-down        Stop infrastructure only"
	@echo "  make backend-verify    Run backend unit + integration tests with JaCoCo"
	@echo "  make frontend-install  Install frontend dependencies"
	@echo "  make frontend-test     Run frontend unit tests (Vitest)"
	@echo "  make frontend-build    Lint and build frontend"
	@echo "  make docker-build      Build core service Docker images"
	@echo "  make compose-validate  Validate all compose files"

up:
	docker compose up -d --build

down:
	docker compose down

infra-up:
	docker compose -f docker-compose.yml up -d

infra-down:
	docker compose -f docker-compose.yml down

backend-verify:
	mvn -B -ntp -f backend/pom.xml clean verify

frontend-install:
	cd frontend/web-app && npm ci

frontend-test:
	cd frontend/web-app && npm run test

frontend-build:
	cd frontend/web-app && npm run lint && npm run build

docker-build:
	docker build -f backend/auth-service/Dockerfile .
	docker build -f backend/profile-service/Dockerfile .
	docker build -f backend/job-service/Dockerfile .
	docker build -f backend/matching-service/Dockerfile .
	docker build -f backend/ai-generation-service/Dockerfile .
	docker build -f backend/document-service/Dockerfile .
	docker build -f backend/workflow-service/Dockerfile .
	docker build -f backend/email-service/Dockerfile .
	docker build -f backend/api-gateway-service/Dockerfile .
	docker build -f frontend/web-app/Dockerfile .

compose-validate:
	docker compose -f docker-compose.yml config >/dev/null
	docker compose -f compose.yaml config >/dev/null
	docker compose -f compose.e2e.yaml config >/dev/null
	docker compose -f docker-compose.full.yml config >/dev/null
