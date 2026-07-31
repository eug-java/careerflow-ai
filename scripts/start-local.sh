#!/usr/bin/env bash
set -euo pipefail

docker compose -f docker-compose.yml up -d

echo "Infrastructure started. Useful commands:"
echo "  make backend-verify   # run backend tests"
echo "  make frontend-test    # run frontend unit tests"
echo "  cd frontend/web-app && npm run dev"
