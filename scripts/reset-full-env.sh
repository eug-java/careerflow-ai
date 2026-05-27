#!/usr/bin/env bash
set -e

docker compose -f docker-compose.full.yml down -v

docker volume prune -f