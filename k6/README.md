# k6 부하 테스트

## 실행 순서
1. **k6 설치 확인**: `k6 version` (없다면 `winget install k6`)
2. **앱 실행 환경 기동**: 레포 루트에서 `docker-compose up -d` (kafka/redis), WAR 빌드 후 로컬 Tomcat에 배포
3. **(선택) Grafana 모니터링 스택 기동**: 결과를 그래프로 보고 싶으면 아래 "Grafana로 결과 확인하기" 참고
4. **BASE_URL 확인**: 배포된 컨텍스트 경로 확인
5. **테스트 실행**:
   ```
   k6 run -e BASE_URL=http://localhost:8080/<context-path> \
          k6/scenarios/<script>.js
   ```
   Grafana로 보려면 `--out influxdb=http://localhost:8086/k6` 옵션을 추가 (3번을 먼저 기동한 경우):
   ```
   k6 run -e BASE_URL=http://localhost:8080/<context-path> \
          --out influxdb=http://localhost:8086/k6 \
          k6/scenarios/<script>.js
   ```

## 디렉토리 구조
- `config.js` — BASE_URL 등 공용 설정
- `scenarios/` — 테스트 스크립트 (엔드포인트/버전별로 파일 분리)
- `results/` — 실행 결과 출력물 (git 추적 제외)
- `docker-compose.yml`, `monitoring.ps1`/`monitoring.sh`, `grafana/` — Grafana 모니터링 스택

## Grafana로 결과 확인하기
k6 결과를 InfluxDB에 적재하고 Grafana 대시보드로 보는 스택 (별도 docker-compose, 앱용 kafka/redis 스택과 무관).

```
# 기동 (Windows PowerShell)
./k6/monitoring.ps1

# 기동 (git bash 등)
./k6/monitoring.sh

# 종료
./k6/monitoring.ps1 down   또는   ./k6/monitoring.sh down
```

기동하면 Grafana(`http://localhost:3000`, 최초 로그인 `admin`/`admin`)에 InfluxDB 데이터소스와
공식 "k6 Load Testing Results" 대시보드(`k6` 폴더)가 자동으로 provisioning 된다. k6 실행 시
`--out influxdb=http://localhost:8086/k6` 옵션만 추가하면 위 대시보드에서 실시간으로 확인 가능.

## 스크립트

### student-v4-concurrency.js
`StudentApiControllerV4`의 락 전략(no-lock/optimistic/pessimistic/redis)별 정원 초과 방지 여부와
응답 시간을 비교. 호출 한 번마다 서버가 내부적으로 `requestCount`개의 스레드로 동시 등록을 재현하고
해당 classroom 상태를 초기화하므로 **반드시 VU=1로 순차 실행**해야 한다 (동시에 여러 VU가 같은
classroomId를 두드리면 결과가 오염됨).

사전조건: `student/jpa-problem.sql`을 실행해 classroom(id=1~3)이 존재해야 함.

```
k6 run -e BASE_URL=http://localhost:8080/<context-path> \
       -e CLASSROOM_ID=1 -e REQUEST_COUNT=50 -e CAPACITY=10 -e REPEAT=5 \
       k6/scenarios/student-v4-concurrency.js
```

optimistic/pessimistic/redis 전략은 `overCapacity==false`를 threshold로 강제하므로, 락이 깨지면
k6가 non-zero exit code로 실패한다. `race`(no-lock)는 정원 초과가 재현되는 게 정상이라 threshold를 걸지 않았다.
