# Profile Service

Profile Service is a core microservice within the CareerFlow AI
platform, responsible for managing candidate profiles.

It handles:

-   basic profile information (name, email, summary)
-   skills
-   work experience

The service exposes a REST API for CRUD operations and is consumed by
other services such as matching and AI generation.

## Tech Stack

-   Java 21\
-   Spring Boot\
-   Spring Data JPA\
-   PostgreSQL\
-   Flyway\
-   Spring Boot Actuator\
-   OpenAPI (Swagger UI)

## Architecture

Client (React) \| API Gateway \| Profile Service \| PostgreSQL

## Getting Started

### 1. Start PostgreSQL

From the project root:

docker compose up -d postgres

### 2. Run the service

cd backend/profile-service mvn spring-boot:run

## Swagger UI

http://localhost:8081/swagger-ui.html

## API Endpoints

Create profile\
POST /api/v1/profiles

Get all profiles\
GET /api/v1/profiles

Get profile by ID\
GET /api/v1/profiles/{id}

Update profile\
PUT /api/v1/profiles/{id}

Delete profile\
DELETE /api/v1/profiles/{id}

## Example Request

{ "fullName": "Evgenii Buianov", "professionalTitle": "Java Software
Engineer", "email": "evgenii@example.com", "phone": "+1 512 000 0000",
"location": "Austin, TX", "summary": "Java Software Engineer with
experience in Spring Boot and microservices.", "skills": \[ { "name":
"Java", "category": "Backend", "yearsOfExperience": 6 }, { "name":
"Spring Boot", "category": "Backend", "yearsOfExperience": 5 } \],
"experiences": \[ { "companyName": "Bank CenterCredit", "positionTitle":
"Java Software Engineer", "location": "Almaty", "startDate":
"2019-01-01", "endDate": "2025-08-01", "currentPosition": false,
"description": "Developed microservices for banking systems." } \] }

## Health Check

GET /actuator/health

## Key Features

-   Database versioning managed with Flyway\
-   Skills and experiences are persisted using cascading\
-   Full replacement of nested entities on update\
-   Input validation for all requests\
-   Centralized error handling via GlobalExceptionHandler



---
---


# Profile Service

Profile Service - это микросервис системы CareerFlow AI, отвечающий за управление профилями кандидатов, включая:

- базовую информацию (имя, email, summary)
- навыки (skills)
- опыт работы (experiences)

Сервис предоставляет REST API для CRUD операций и используется другими сервисами (matching, AI generation).

---

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway
- Spring Boot Actuator
- OpenAPI (Swagger UI)

---

## Архитектура

```text
Client (React)
      |
API Gateway
      |
Profile Service
      |
PostgreSQL
```

---

###Запуск

##1. Поднять PostgreSQL

В корне проекта:

docker compose up -d postgres

##2. Запустить сервис
cd backend/profile-service
mvn spring-boot:run

---

##Swagger UI

http://localhost:8081/swagger-ui.html

##API Endpoints
Создать профиль
POST /api/v1/profiles
Получить все профили
GET /api/v1/profiles
Получить профиль по id
GET /api/v1/profiles/{id}
Обновить профиль
PUT /api/v1/profiles/{id}
Удалить профиль
DELETE /api/v1/profiles/{id}

##Пример запроса
{
  "fullName": "Evgenii Buianov",
  "professionalTitle": "Java Software Engineer",
  "email": "evgenii@example.com",
  "phone": "+1 512 000 0000",
  "location": "Austin, TX",
  "summary": "Java Software Engineer with experience in Spring Boot and microservices.",
  "skills": [
    {
      "name": "Java",
      "category": "Backend",
      "yearsOfExperience": 6
    },
    {
      "name": "Spring Boot",
      "category": "Backend",
      "yearsOfExperience": 5
    }
  ],
  "experiences": [
    {
      "companyName": "Bank CenterCredit",
      "positionTitle": "Java Software Engineer",
      "location": "Almaty",
      "startDate": "2019-01-01",
      "endDate": "2025-08-01",
      "currentPosition": false,
      "description": "Developed microservices for banking systems."
    }
  ]
}

##Health Check
GET /actuator/health

---

##Особенности
Flyway используется для миграций БД
Skills и Experiences сохраняются каскадно
Полная замена вложенных данных при update
Валидация входящих данных
Единый формат ошибок через GlobalExceptionHandler
