# Unit Tests

## Recommended dependencies

Each backend service should have:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

If Mockito JUnit Jupiter is not included in your setup, add:

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

## Run all tests

```bash
cd backend
mvn test
```

## Run one service

```bash
cd backend/profile-service
mvn test
```

## Run one test class

```bash
cd backend
mvn -pl profile-service -Dtest=ProfileServiceTest test
```
