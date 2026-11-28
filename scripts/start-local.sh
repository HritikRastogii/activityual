#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
if [[ ! -f .env ]]; then
  echo "Missing .env. Run: cp env.example .env and configure local values."
  exit 1
fi
docker compose up -d
echo "Local PostgreSQL and DynamoDB Local are starting."
docker compose ps
echo "Start the five backend services and frontend in separate terminals; see README.md."
