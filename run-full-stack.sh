#!/usr/bin/env bash
# mysql/kafka/redis/grafana 모니터링 스택 + 앱 WAR를 한 번에 docker로 띄우고
# k6 부하 테스트까지 실행한 뒤 컨테이너를 정리한다.
set -euo pipefail

# git bash(MSYS)가 "/scripts/..." 같은 컨테이너 내부 경로를 윈도우 경로로 잘못 변환하는 것을 방지
export MSYS_NO_PATHCONV=1

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

SCENARIO="${SCENARIO:-scenarios/student-v4-concurrency.js}"
CLASSROOM_ID="${CLASSROOM_ID:-1}"
REQUEST_COUNT="${REQUEST_COUNT:-50}"
CAPACITY="${CAPACITY:-10}"
REPEAT="${REPEAT:-5}"
APP_READY_TIMEOUT="${APP_READY_TIMEOUT:-180}"

K6_EXIT_CODE=0

# mysql 헬스체크가 초기 부트스트랩(임시 기동 후 재시작) 도중 healthy로 보고하는 경우가 있어
# 실제 접속 가능해질 때까지 재시도한다.
wait_for_mysql() {
    local tries=0
    until docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p1234 -e "SELECT 1" >/dev/null 2>&1; do
        tries=$((tries + 1))
        if [ "$tries" -ge 30 ]; then
            echo "mysql에 접속할 수 없습니다." >&2
            return 1
        fi
        sleep 2
    done
}

cleanup() {
    echo "[cleanup] 컨테이너 종료 중..."
    docker compose down
}
trap cleanup EXIT

echo "[1/6] 이전 실행 정리 (mysql/kafka/redis/influxdb 데이터 초기화)"
docker compose down -v --remove-orphans || true

echo "[2/6] 인프라 기동 (mysql, kafka, redis, influxdb, grafana)"
docker compose up -d --build --wait mysql kafka redis influxdb grafana

echo "[3/6] 기본 스키마 적재 (member, student)"
wait_for_mysql
docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p1234 \
    < src/main/java/org/example/kb7spring/member/db.sql
docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p1234 kb7spring \
    < src/main/java/org/example/kb7spring/student/db.sql

echo "[4/6] 앱 기동 (Hibernate가 classroom 테이블/classroom_id 컬럼을 자동 생성)"
docker compose up -d --build --wait app

echo "[4/6] 앱 헬스체크 대기 (Tomcat 'Server startup' 로그 확인)"
elapsed=0
until docker compose logs app 2>&1 | grep -q "Server startup"; do
    if [ "$elapsed" -ge "$APP_READY_TIMEOUT" ]; then
        echo "앱이 ${APP_READY_TIMEOUT}초 내에 기동되지 않았습니다. 'docker compose logs app'으로 확인하세요." >&2
        exit 1
    fi
    sleep 2
    elapsed=$((elapsed + 2))
done
echo "앱 기동 완료"

echo "[5/6] 부하 테스트용 시드 데이터 적재 (classroom/student)"
docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p1234 kb7spring \
    < src/main/java/org/example/kb7spring/student/jpa-problem.sql

echo "[6/6] k6 테스트 실행: $SCENARIO"
set +e
docker compose --profile test run --rm k6 run \
    -e BASE_URL=http://app:8080 \
    -e CLASSROOM_ID="$CLASSROOM_ID" \
    -e REQUEST_COUNT="$REQUEST_COUNT" \
    -e CAPACITY="$CAPACITY" \
    -e REPEAT="$REPEAT" \
    --out influxdb=http://influxdb:8086/k6 \
    "/scripts/$SCENARIO"
K6_EXIT_CODE=$?
set -e

exit "$K6_EXIT_CODE"
