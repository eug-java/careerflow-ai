FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn -B -ntp -f backend/pom.xml clean package -DskipTests -pl :api-gateway-service -am

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/backend/api-gateway-service/target/*-exec.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
