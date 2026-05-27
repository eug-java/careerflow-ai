# CareerFlow AI - System Design

## 1. Problem

CareerFlow AI is an AI-powered job application automation platform.

The system helps users:

- Store candidate profile data
- Store job descriptions
- Parse raw job descriptions with AI
- Generate tailored resumes and cover letters
- Track async document generation workflows
- Preview and download generated documents as PDF or DOCX

## 2. Functional Requirements

### Profile Management

Users can:

- Create profile
- View profiles
- Edit profile
- Delete profile

### Job Management

Users can:

- Create job manually
- Parse raw job description with AI
- View jobs
- Edit job
- Delete job

### Document Generation

Users can:

- Select profile
- Select job
- Generate resume
- Generate cover letter
- Track workflow status
- Preview generated document
- Download PDF
- Download DOCX
- Delete generated document

### Authentication

Users can:

- Login
- Access protected APIs with JWT

## 3. Non-Functional Requirements

- Modular microservice architecture
- Async processing for document generation
- Event-driven communication
- Secure API access
- Durable document storage
- Observable services
- Extensible AI integration
- Frontend-friendly API gateway
- Local development with Docker Compose

## 4. High-Level Architecture

```text
React Frontend
      |
      v
Spring Cloud Gateway
      |
      +--------------------+--------------------+
      |                    |                    |
      v                    v                    v
Auth Service        Profile Service       Job Service
      |                    |                    |
      |                    +---------+----------+
      |                              |
      v                              v
JWT                         Workflow Service
                                  |
                                  v
                            Camunda 8
                                  |
                                  v
                         AI Generation Service
                                  |
                                  v
                                Kafka
                                  |
                                  v
                           Document Service
                                  |
                         +--------+--------+
                         |                 |
                         v                 v
                    PostgreSQL           MinIO
```

# 5. Service Responsibilities
   ## api-gateway-service

Responsible for:

- Central API entry point
- Routing requests to backend services
- JWT validation
- CORS configuration
- WebSocket proxying

## auth-service

Responsible for:

- Demo login
- JWT issuing
- Token expiration configuration

Current MVP uses hardcoded demo credentials:
```text
demo / demo
```

Future version should use database-backed users and password hashing.

## profile-service

Responsible for:

- Candidate profile CRUD
- Profile data used for AI generation

##job-service

Responsible for:

- Job description CRUD
- Job skill storage
- Parsed job data persistence

## ai-generation-service

Responsible for:

- Resume generation
- Cover letter generation
- Raw job description parsing
- Spring AI / OpenAI integration
- Fallback deterministic generation

## workflow-service

Responsible for:

Starting Camunda workflow instances
Running workers
Calling AI generation
Publishing document events
Tracking workflow status
Sending WebSocket status updates

## document-service

Responsible for:

- Consuming generated document events
- Saving metadata to PostgreSQL
- Saving markdown content to MinIO
- Returning document content
- Exporting PDF
- Exporting DOCX
- Deleting documents from PostgreSQL and MinIO

# 6. Main Document Generation Flow

```text
1. User logs in
2. User selects profile
3. User selects job
4. User clicks Generate Resume or Generate Cover Letter
5. Frontend calls workflow-service through API Gateway
6. workflow-service starts Camunda process
7. generate-document worker calls ai-generation-service
8. ai-generation-service returns generated markdown
9. publish-document-event worker publishes Kafka event
10. document-service consumes event
11. document-service saves content to MinIO
12. document-service saves metadata to PostgreSQL
13. workflow-service marks status COMPLETED
14. frontend receives WebSocket status update
15. user previews or downloads the document
```

# 7. Why Camunda

Camunda is used for orchestration because document generation is a multi-step business process.

Benefits:

Clear process visibility
BPMN model for business workflow
Worker-based execution
Retry and incident handling support
Better fit for long-running workflows than direct REST chaining
# 8. Why Kafka

Kafka is used between workflow-service and document-service.

Benefits:

- Async document persistence
- Loose coupling
- Event-driven architecture
- Retry/replay potential
- Better resilience if document-service is temporarily unavailable
# 9. Why MinIO

Generated documents are stored in MinIO because document content is object-like data.

Benefits:

- S3-compatible storage
- Better fit for generated files than relational columns
- Easy future migration to AWS S3
- Metadata remains in PostgreSQL
# 10. Why API Gateway

The frontend talks only to API Gateway.

Benefits:

- Single API endpoint
- Centralized JWT validation
- Centralized CORS handling
- Hides internal service ports
- Simplifies frontend configuration
- Supports WebSocket proxying
# 11. Security Design

Current MVP:

- JWT access token
- Gateway validates JWT
- Internal services are not directly secured yet
- Demo credentials are hardcoded

Future improvements:

- Refresh tokens
- Password hashing with BCrypt
- Database-backed users
- RBAC roles
- Service-to-service authentication
- Token revocation
- HTTPS
# 12. Observability

Current / planned observability:

- Spring Boot Actuator
- Micrometer
- Prometheus metrics
- Grafana dashboards
- Application health checks

Future improvements:

- OpenTelemetry tracing
- Jaeger distributed tracing
- Correlation IDs
- Centralized logs
# 13. Resilience

Current MVP:

- Async event-driven document persistence
- Fallback AI generation
- Polling fallback for WebSocket status

Future improvements:

- Resilience4j retries
- Circuit breakers
- Timeouts
- Dead-letter topic
- Outbox pattern
- Idempotent event consumers
# 14. Data Storage
   ### PostgreSQL

Used for:

- Profiles
- Jobs
- Matching results
- Document metadata
### MinIO

Used for:

- Generated markdown documents
- Future uploaded resume files
### Kafka

Used for:

- Document generation events
# 15. Frontend Design

The frontend provides:

- Login page
- Profiles CRUD
- Jobs CRUD
- AI job parsing
- Document generation
- Workflow status tracking
- Document preview
- PDF and DOCX downloads
# 16. Trade-Offs
   ### Current MVP Uses In-Memory Workflow Status

Pros:

- Simple
- Fast to implement
- Good for local demo

Cons:

- Status is lost on restart
- Not horizontally scalable

Production improvement:

- Store workflow status in PostgreSQL or Redis
### Current Auth Uses Hardcoded User

Pros:

- Good for portfolio demo
- Simple local setup

Cons:

- Not production-ready

Production improvement:

- Add user table, BCrypt password hashing, refresh tokens
### Kafka Event Without Outbox Pattern

Pros:

- Simple event flow
- Easy demo

Cons:

- Possible consistency issue between DB and Kafka in future flows

Production improvement:

- Transactional outbox pattern
# 17. Future Roadmap
   - Kubernetes deployment
   - GitHub Actions CI/CD
   - OpenTelemetry + Jaeger tracing
   - Redis caching
   - Resilience4j
   - Real user registration
   - Refresh tokens
   - RBAC
   - Email tracking integration
   - Resume upload and parsing
   - Vector search and embeddings
   - ATS score improvements
   - Multi-tenant support