#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./mvnw -U -B clean verify
(cd frontend && npm ci && npm test && npm run build)
