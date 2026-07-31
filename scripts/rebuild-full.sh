#!/bin/bash

docker compose down
cd backend
mvn clean package -DskipTests
cd ..
docker compose up -d --build