#!/usr/bin/env bash
set -e

docker compose down -v

docker volume prune -f