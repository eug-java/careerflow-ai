# CareerFlow AI - Interview Notes

## 1. 30-Second Pitch

CareerFlow AI is an AI-powered job application automation platform.

It allows a user to create profiles, add job descriptions, parse raw job descriptions with AI, generate tailored resumes and cover letters, preview the generated documents, and download them as PDF or DOCX.

The system is built as a Java 21 microservice architecture with Spring Boot, Spring Cloud Gateway, Camunda 8, Kafka, PostgreSQL, MinIO, Spring AI, OpenAI, and React.

## 2. 2-Minute Architecture Explanation

The frontend is built with React and TypeScript.

All frontend traffic goes through a reactive Spring Cloud Gateway. The gateway handles routing, CORS, JWT validation, and WebSocket proxying.

The backend is split into several services:

- auth-service issues JWT tokens.
- profile-service manages candidate profiles.
- job-service manages job descriptions.
- ai-generation-service uses Spring AI and OpenAI to generate resumes, cover letters, and parse raw job descriptions.
- workflow-service orchestrates document generation using Camunda 8.
- document-service stores generated documents in MinIO and metadata in PostgreSQL.

For async communication, workflow-service publishes a Kafka event after the document content is generated. document-service consumes the event and stores the document.

The frontend tracks workflow status using WebSocket updates with polling fallback.

## 3. 10-Minute Deep Dive

### Step 1 - User Interaction

The user logs in with demo credentials and receives a JWT token.

The frontend stores the token and sends it in the Authorization header for protected API calls.

### Step 2 - API Gateway

All requests go through api-gateway-service.

The gateway validates JWT tokens, handles CORS, and routes requests to internal services.

### Step 3 - Profile and Job Management

The user can create, edit, and delete profiles and jobs.

Job descriptions can be added manually or parsed from raw text using AI.

### Step 4 - AI Parsing

For raw job descriptions, the frontend sends text to ai-generation-service.

The AI service extracts structured fields:

- job title
- company
- location
- employment type
- salary
- remote flag
- skills
- clean description

The user can review and edit the parsed result before saving.

### Step 5 - Workflow Start

When the user clicks Generate Resume or Generate Cover Letter, the frontend calls workflow-service.

workflow-service starts a Camunda process instance with:

- profileId
- jobId
- documentType

### Step 6 - Camunda Workers

The first worker calls ai-generation-service to generate the document content.

The second worker publishes a Kafka event with generated content.

### Step 7 - Kafka Event

Kafka decouples workflow-service from document-service.

This means document-service can process the generated document asynchronously.

### Step 8 - Document Storage

document-service consumes the Kafka event.

It stores metadata in PostgreSQL and markdown content in MinIO.

### Step 9 - Frontend Status

workflow-service tracks workflow status:

- RUNNING
- COMPLETED
- FAILED

The frontend receives status updates through WebSocket and uses polling as fallback.

### Step 10 - Document Preview and Export

The user can preview document content.

The user can download the generated document as:

- PDF
- DOCX

## 4. Key Design Decisions

### Why Microservices?

The project separates responsibilities clearly:

- Auth
- Profiles
- Jobs
- AI generation
- Workflow orchestration
- Document storage

This makes the architecture easier to scale and explain.

### Why Camunda?

Camunda is used because document generation is a business workflow, not just a single API call.

It gives:

- clear process modeling
- worker-based execution
- future retry and incident handling
- visibility into long-running processes

### Why Kafka?

Kafka decouples document generation from document persistence.

If document-service is down temporarily, the event can still be consumed later.

### Why MinIO?

Generated documents are file-like objects.

MinIO is S3-compatible, so the project can later move to AWS S3 with minimal changes.

### Why API Gateway?

The gateway gives one entry point for frontend and centralizes cross-cutting concerns:

- authentication
- CORS
- routing
- WebSocket proxying

### Why WebSocket + Polling?

WebSocket gives fast live updates.

Polling is kept as fallback because WebSocket events can be missed if the connection reconnects or the browser tab refreshes.

## 5. Production Gaps and How I Would Improve Them

### Current Limitation: In-Memory Workflow Status

Current MVP stores workflow status in memory.

Production improvement:

- Store workflow status in PostgreSQL or Redis.
- Add expiration/cleanup.
- Make it horizontally scalable.

### Current Limitation: Hardcoded Demo User

Current MVP uses demo credentials.

Production improvement:

- Add users table.
- Store BCrypt password hashes.
- Add refresh tokens.
- Add RBAC.

### Current Limitation: No Distributed Tracing Yet

Production improvement:

- Add OpenTelemetry.
- Add Jaeger.
- Add correlation IDs across services and Kafka events.

### Current Limitation: Kafka Without Outbox

Production improvement:

- Add transactional outbox pattern.
- Add dead-letter topic.
- Add idempotency keys for consumers.

### Current Limitation: AI Reliability

Production improvement:

- Add structured output validation.
- Add retry and circuit breaker.
- Add prompt versioning.
- Add AI audit logs.

## 6. Interview Questions and Answers

### Q: Why did you use Camunda instead of direct REST calls?

Because document generation is a multi-step business process. Camunda gives process visibility, retry support, worker orchestration, and makes the flow easier to evolve.

### Q: Why did you use Kafka?

Kafka decouples workflow-service from document-service. The workflow publishes an event and document-service processes it asynchronously. This improves resilience and avoids tight coupling.

### Q: What happens if document-service is down?

The current MVP depends on Kafka retention. In production I would add retry configuration, consumer error handling, a dead-letter topic, and idempotent processing.

### Q: How do you avoid duplicate documents?

In production I would add an idempotency key, for example processInstanceKey + documentType, and enforce uniqueness in document-service.

### Q: How do you secure the system?

Currently the gateway validates JWT tokens. In production I would add user registration, BCrypt password hashing, refresh tokens, RBAC, HTTPS, and service-to-service authentication.

### Q: How would you scale this system?

I would scale stateless services horizontally, use Kafka consumer groups for document-service, move workflow status to Redis/PostgreSQL, use S3 instead of local MinIO, and deploy to Kubernetes.

### Q: How do you monitor the system?

The project is prepared for Actuator, Micrometer, Prometheus, and Grafana. I would also add OpenTelemetry and Jaeger for distributed tracing.

### Q: What was the hardest part?

The hardest part was integrating async workflow orchestration, Kafka events, document persistence, and live frontend status updates into one end-to-end flow.

### Q: What would you improve next?

I would add persistent workflow status, Resilience4j, OpenTelemetry tracing, GitHub Actions CI/CD, and Kubernetes deployment.

## 7. STAR Story

### Situation

I wanted to build a portfolio project that demonstrates real backend engineering, not just CRUD.

### Task

The goal was to create an AI-powered platform that automates resume and cover letter generation based on user profiles and job descriptions.

### Action

I designed a microservice architecture with Spring Boot, Camunda 8, Kafka, PostgreSQL, MinIO, Spring AI, and React.

I added JWT authentication, a reactive API gateway, AI parsing, workflow orchestration, Kafka-based document persistence, WebSocket status updates, and PDF/DOCX export.

### Result

The result is a production-style fullstack platform that demonstrates distributed systems, AI integration, workflow orchestration, event-driven architecture, and modern frontend/backend integration.

## 8. Best Short Resume Bullet

Built CareerFlow AI, a Java 21 microservice platform that uses Spring Boot, Camunda 8, Kafka, Spring AI, PostgreSQL, MinIO, and React to generate tailored resumes and cover letters with async workflow orchestration and real-time status tracking.

## 9. Best LinkedIn Description

CareerFlow AI is a production-style AI job application automation platform built with Java 21, Spring Boot, Spring Cloud Gateway, Camunda 8, Kafka, PostgreSQL, MinIO, Spring AI, OpenAI, and React.

It supports profile and job management, AI-powered job parsing, resume and cover letter generation, Kafka-based async document storage, JWT authentication, WebSocket workflow status updates, and PDF/DOCX exports.