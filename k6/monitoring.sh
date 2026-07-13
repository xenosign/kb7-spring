#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ACTION="${1:-up}"

if [ "$ACTION" = "down" ]; then
    docker compose -f "$DIR/docker-compose.yml" down
    exit 0
fi

docker compose -f "$DIR/docker-compose.yml" up -d

cat <<EOF

Grafana : http://localhost:3000 (admin/admin, 최초 로그인 시 비밀번호 변경)
InfluxDB: http://localhost:8086 (database: k6)

k6 실행 시 --out influxdb=http://localhost:8086/k6 옵션을 추가하면 Grafana의
'k6' 폴더 > 'k6 Load Testing Results' 대시보드에서 결과를 확인할 수 있습니다.
EOF
