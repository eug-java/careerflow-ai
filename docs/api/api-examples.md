
# CareerFlow AI - API Examples

## Base URL

```text
http://localhost:8080
```

All requests go through API Gateway.

---

# 1. Authentication

## Login

### Request

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo",
    "password": "demo"
  }'
```

### Response

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

## Save Token

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo"}' | jq -r '.accessToken')
```

---

# 2. Profiles API

## Get All Profiles

```bash
curl http://localhost:8080/api/v1/profiles \
  -H "Authorization: Bearer $TOKEN"
```

---

## Create Profile

```bash
curl -X POST http://localhost:8080/api/v1/profiles \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Evgenii Buianov",
    "professionalTitle": "Java Software Engineer",
    "email": "evgenii@example.com",
    "phone": "+15120000000",
    "location": "Austin, TX",
    "summary": "Java Software Engineer with Spring Boot, Kafka, PostgreSQL, Camunda and AWS experience."
  }'
```

---

## Get Profile By ID

```bash
PROFILE_ID=YOUR_PROFILE_ID

curl http://localhost:8080/api/v1/profiles/$PROFILE_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

## Update Profile

```bash
curl -X PUT http://localhost:8080/api/v1/profiles/$PROFILE_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Evgenii Buianov",
    "professionalTitle": "Senior Java Software Engineer",
    "email": "evgenii@example.com",
    "phone": "+15120000000",
    "location": "Austin, TX",
    "summary": "Updated profile summary."
  }'
```

---

## Delete Profile

```bash
curl -X DELETE http://localhost:8080/api/v1/profiles/$PROFILE_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

# 3. Jobs API

## Get All Jobs

```bash
curl http://localhost:8080/api/v1/jobs \
  -H "Authorization: Bearer $TOKEN"
```

---

## Create Job

```bash
curl -X POST http://localhost:8080/api/v1/jobs \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Senior Java Developer",
    "companyName": "Dell",
    "location": "Austin, TX",
    "employmentType": "Full-time",
    "salaryMin": 120000,
    "salaryMax": 150000,
    "currency": "USD",
    "remote": false,
    "description": "Java backend engineering role.",
    "skills": [
      {
        "name": "Java",
        "required": true
      },
      {
        "name": "Spring Boot",
        "required": true
      },
      {
        "name": "Kafka",
        "required": true
      }
    ]
  }'
```

---

## Get Job By ID

```bash
JOB_ID=YOUR_JOB_ID

curl http://localhost:8080/api/v1/jobs/$JOB_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

## Update Job

```bash
curl -X PUT http://localhost:8080/api/v1/jobs/$JOB_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Lead Java Developer",
    "companyName": "Dell",
    "location": "Austin, TX",
    "employmentType": "Full-time",
    "salaryMin": 140000,
    "salaryMax": 170000,
    "currency": "USD",
    "remote": true,
    "description": "Updated job description.",
    "skills": [
      {
        "name": "Java",
        "required": true
      },
      {
        "name": "Spring Boot",
        "required": true
      }
    ]
  }'
```

---

## Delete Job

```bash
curl -X DELETE http://localhost:8080/api/v1/jobs/$JOB_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

# 4. AI Job Parsing API

## Parse Raw Job Description

```bash
curl -X POST http://localhost:8080/api/v1/generations/jobs/parse \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Senior Java Developer at Dell in Austin, TX. Looking for Java, Spring Boot, Kafka, PostgreSQL, Docker, Kubernetes and AWS experience. Salary range is 120000 to 150000 USD."
  }'
```

### Example Response

```json
{
  "title": "Senior Java Developer",
  "companyName": "Dell",
  "location": "Austin, TX",
  "employmentType": "Full-time",
  "salaryMin": 120000,
  "salaryMax": 150000,
  "currency": "USD",
  "remote": false,
  "description": "Senior Java backend engineering role.",
  "skills": [
    {
      "name": "Java",
      "required": true
    },
    {
      "name": "Spring Boot",
      "required": true
    }
  ]
}
```

---

# 5. Workflow API

## Start Resume Generation Workflow

```bash
curl -X POST http://localhost:8080/api/v1/workflows/document-generation \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "profileId": "PROFILE_ID",
    "jobId": "JOB_ID",
    "documentType": "RESUME"
  }'
```

---

## Start Cover Letter Generation Workflow

```bash
curl -X POST http://localhost:8080/api/v1/workflows/document-generation \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "profileId": "PROFILE_ID",
    "jobId": "JOB_ID",
    "documentType": "COVER_LETTER"
  }'
```

### Example Response

```json
{
  "processInstanceKey": 2251799813685350,
  "status": "STARTED"
}
```

---

## Check Workflow Status

```bash
PROCESS_INSTANCE_KEY=2251799813685350

curl http://localhost:8080/api/v1/workflows/status/$PROCESS_INSTANCE_KEY \
  -H "Authorization: Bearer $TOKEN"
```

### Example Response

```json
{
  "processInstanceKey": 2251799813685350,
  "status": "COMPLETED"
}
```

---

# 6. Documents API

## Get All Documents

```bash
curl http://localhost:8080/api/v1/documents \
  -H "Authorization: Bearer $TOKEN"
```

---

## Get Document Content

```bash
DOCUMENT_ID=YOUR_DOCUMENT_ID

curl http://localhost:8080/api/v1/documents/$DOCUMENT_ID/content \
  -H "Authorization: Bearer $TOKEN"
```

---

## Download PDF

```bash
curl -L http://localhost:8080/api/v1/documents/$DOCUMENT_ID/pdf \
  -H "Authorization: Bearer $TOKEN" \
  -o document.pdf
```

---

## Download DOCX

```bash
curl -L http://localhost:8080/api/v1/documents/$DOCUMENT_ID/docx \
  -H "Authorization: Bearer $TOKEN" \
  -o document.docx
```

---

## Delete Document

```bash
curl -X DELETE http://localhost:8080/api/v1/documents/$DOCUMENT_ID \
  -H "Authorization: Bearer $TOKEN"
```

---

# 7. Health Checks

## Gateway

```bash
curl http://localhost:8080/actuator/health
```

---

## Profile Service

```bash
curl http://localhost:8081/actuator/health
```

---

## Job Service

```bash
curl http://localhost:8082/actuator/health
```

---

## AI Generation Service

```bash
curl http://localhost:8084/actuator/health
```

---

## Document Service

```bash
curl http://localhost:8085/actuator/health
```

---

## Workflow Service

```bash
curl http://localhost:8086/actuator/health
```

---

# 8. Kafka Commands

## List Topics

```bash
docker exec -it careerflow-kafka /opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server localhost:9092 \
  --list
```

---

## Read document.generated Topic

```bash
docker exec -it careerflow-kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic document.generated \
  --from-beginning
```

---

# 9. MinIO

## Open Console

```text
http://localhost:9001
```

## Default Bucket

```text
careerflow-documents
```

---

# 10. Frontend

## Start Frontend

```bash
cd frontend/web-app
npm install
npm run dev
```

## Frontend URL

```text
http://localhost:5173
```

