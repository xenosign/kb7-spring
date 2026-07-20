# k6 부하 테스트

## 한 번에 실행하기 (권장)
레포 루트의 `run-full-stack.sh`(git bash) / `run-full-stack.ps1`(PowerShell)이 mysql/kafka/redis/influxdb/grafana +
앱 WAR 컨테이너 기동 → 헬스체크 대기 → 시드 데이터 적재 → k6 컨테이너 실행 → 전체 종료까지 한 번에 처리한다.

```
# git bash 등
./run-full-stack.sh

# Windows PowerShell
./run-full-stack.ps1
```

시나리오/파라미터는 환경변수로 override 가능 (기본값은 `student-v4-concurrency.js` 예시와 동일):

```
SCENARIO=scenarios/student-v4-concurrency.js CLASSROOM_ID=1 REQUEST_COUNT=50 CAPACITY=10 REPEAT=5 ./run-full-stack.sh
```

실행마다 `docker compose down -v`로 이전 mysql/kafka/redis/influxdb 데이터를 초기화한 뒤 새로 띄우고,
테스트가 끝나면(성공/실패 무관) app/mysql/kafka/redis 컨테이너만 정리한다. influxdb/grafana는 계속 떠 있으므로
k6 결과는 테스트 종료 후에도 Grafana(`http://localhost:3000`)에서 확인 가능하다. 완전히 정리하려면
`docker compose down`(볼륨까지 지우려면 `-v` 추가)을 직접 실행한다.

## 개별 단계로 실행하기 (디버깅용)
1. **k6 설치 확인**: `k6 version` (없다면 `winget install k6`)
2. **인프라 기동**: 레포 루트에서 `docker compose up -d --wait mysql kafka redis influxdb grafana`
3. **기본 스키마 적재** (반드시 앱 기동 전에, `app`이 없으면 hibernate.hbm2ddl.auto=update가 classroom 테이블을 만들 수 없음):
   ```
   docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p1234 < src/main/java/org/example/kb7spring/member/db.sql
   docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p1234 kb7spring < src/main/java/org/example/kb7spring/student/db.sql
   ```
4. **앱 기동**: `docker compose up -d --build --wait app`
   (WAR가 자동으로 빌드되어 `app` 컨테이너의 Tomcat에 ROOT로 배포됨, `http://localhost:18080/`. 기동 시 Hibernate가
   `classroom` 테이블과 `student.classroom_id` 컬럼을 자동 생성)
5. **부하 테스트용 시드 데이터 적재**: `docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p1234 kb7spring < src/main/java/org/example/kb7spring/student/jpa-problem.sql`
6. **테스트 실행** (로컬 k6 바이너리로 호스트에서 직접 실행하는 경우):
   ```
   k6 run -e BASE_URL=http://localhost:18080 \
          -e CLASSROOM_ID=1 -e REQUEST_COUNT=50 -e CAPACITY=10 -e REPEAT=5 \
          k6/scenarios/student-v4-concurrency.js --out influxdb=http://localhost:8086/k6
   ```
   또는 k6도 컨테이너로 실행:
   ```
   docker compose --profile test run --rm k6 run \
       -e BASE_URL=http://app:8080 -e CLASSROOM_ID=1 -e REQUEST_COUNT=50 -e CAPACITY=10 -e REPEAT=5 \
       --out influxdb=http://influxdb:8086/k6 /scripts/scenarios/student-v4-concurrency.js
   ```
7. **종료**: `docker compose down` (데이터까지 초기화하려면 `-v` 추가)

## 디렉토리 구조
- `config.js` — BASE_URL 등 공용 설정
- `scenarios/` — 테스트 스크립트 (엔드포인트/버전별로 파일 분리)
- `results/` — 실행 결과 출력물 (git 추적 제외)
- `grafana/` — Grafana 프로비저닝 설정 (레포 루트 `docker-compose.yml`의 `grafana`/`influxdb` 서비스가 참조)

Grafana(`http://localhost:3000`, 최초 로그인 `admin`/`admin`)에는 InfluxDB 데이터소스와 공식
"k6 Load Testing Results" 대시보드(`k6` 폴더)가 자동으로 provisioning 되어 있다. k6 실행 시
`--out influxdb=http://localhost:8086/k6`(호스트 실행) 또는 `influxdb=http://influxdb:8086/k6`(컨테이너 실행)
옵션만 추가하면 위 대시보드에서 실시간으로 확인 가능.

## 스크립트

### student-v4-concurrency.js
`StudentApiControllerV4`의 락 전략(no-lock/optimistic/pessimistic/redis)별 정원 초과 방지 여부와
응답 시간을 비교. 호출 한 번마다 서버가 내부적으로 `requestCount`개의 스레드로 동시 등록을 재현하고
해당 classroom 상태를 초기화하므로 **반드시 VU=1로 순차 실행**해야 한다 (동시에 여러 VU가 같은
classroomId를 두드리면 결과가 오염됨).

사전조건: `student/jpa-problem.sql`을 실행해 classroom(id=1~3)이 존재해야 함.

```
k6 run -e BASE_URL=http://localhost:18080/<context-path> \
       -e CLASSROOM_ID=1 -e REQUEST_COUNT=50 -e CAPACITY=10 -e REPEAT=5 \
       k6/scenarios/student-v4-concurrency.js --out influxdb=http://localhost:8086/k6
```

optimistic/pessimistic/redis 전략은 `overCapacity==false`를 threshold로 강제하므로, 락이 깨지면
k6가 non-zero exit code로 실패한다. `race`(no-lock)는 정원 초과가 재현되는 게 정상이라 threshold를 걸지 않았다.

## 최종 결과 확인용 - 그라파나만 다시 띄워서 확인
docker compose up -d influxdb grafana