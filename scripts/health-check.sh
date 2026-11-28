#!/usr/bin/env bash
set -u
check() {
  local name="$1" url="$2"
  if curl -fsS --max-time 4 "$url" >/dev/null; then
    printf 'HEALTHY  %s  %s\n' "$name" "$url"
  else
    printf 'UNAVAILABLE  %s  %s\n' "$name" "$url"
  fi
}
check auth http://localhost:8081/actuator/health
check activity http://localhost:8082/actuator/health
check tracking http://localhost:8083/actuator/health
check analytics http://localhost:8084/actuator/health
check recommendation http://localhost:8085/actuator/health
check dynamodb-local http://localhost:8000
