#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

echo "Project root: $PROJECT_ROOT"

echo "Stopping local infrastructure compose..."
docker compose -f docker-compose.yml down --remove-orphans || true

echo "Stopping full dockerized compose..."
docker compose -f docker-compose.full.yml down --remove-orphans || true

echo "Stopping known CareerFlow containers if still running..."
docker ps -a --format '{{.Names}}' | grep '^careerflow-' | xargs -r docker stop || true
docker ps -a --format '{{.Names}}' | grep '^careerflow-' | xargs -r docker rm || true

echo "Removing dangling CareerFlow networks..."
docker network ls --format '{{.Name}}' | grep '^careerflow-ai' | xargs -r docker network rm || true

echo "Checking occupied ports..."
for port in 8080 8079 8081 8082 8083 8084 8085 8086 5173 5432 5433 5434 5435 55432 55433 55434 55435 9000 9001 9092 9090 3001; do
  if sudo lsof -i :"$port" >/dev/null 2>&1; then
    echo "Port $port is still in use:"
    sudo lsof -i :"$port"
  fi
done

echo "Done."