#!/bin/bash

docker compose -f docker-compose.full.yml down
cd backend
mvn clean package -DskipTests
cd ..
docker compose -f docker-compose.full.yml up --build