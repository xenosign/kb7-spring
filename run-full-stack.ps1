# mysql/kafka/redis/grafana 모니터링 스택 + 앱 WAR를 한 번에 docker로 띄우고
# k6 부하 테스트까지 실행한 뒤 컨테이너를 정리한다.
$ErrorActionPreference = 'Stop'

Set-Location $PSScriptRoot

$Scenario = if ($env:SCENARIO) { $env:SCENARIO } else { 'scenarios/student-v4-concurrency.js' }
$ClassroomId = if ($env:CLASSROOM_ID) { $env:CLASSROOM_ID } else { '1' }
$RequestCount = if ($env:REQUEST_COUNT) { $env:REQUEST_COUNT } else { '50' }
$Capacity = if ($env:CAPACITY) { $env:CAPACITY } else { '10' }
$Repeat = if ($env:REPEAT) { $env:REPEAT } else { '5' }
$AppReadyTimeout = if ($env:APP_READY_TIMEOUT) { [int]$env:APP_READY_TIMEOUT } else { 180 }

$k6ExitCode = 0

function Wait-ForMysql {
    $tries = 0
    while ($true) {
        docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p1234 -e "SELECT 1" *> $null
        if ($LASTEXITCODE -eq 0) { return }
        $tries++
        if ($tries -ge 30) { throw "mysql에 접속할 수 없습니다." }
        Start-Sleep -Seconds 2
    }
}

try {
    Write-Host "[1/6] 이전 실행 정리 (mysql/kafka/redis/influxdb 데이터 초기화)"
    try { docker compose down -v --remove-orphans } catch {}

    Write-Host "[2/6] 인프라 기동 (mysql, kafka, redis, influxdb, grafana)"
    docker compose up -d --build --wait mysql kafka redis influxdb grafana

    Write-Host "[3/6] 기본 스키마 적재 (member, student)"
    Wait-ForMysql
    Get-Content 'src/main/java/org/example/kb7spring/member/db.sql' -Raw |
        docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p1234
    Get-Content 'src/main/java/org/example/kb7spring/student/db.sql' -Raw |
        docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p1234 kb7spring

    Write-Host "[4/6] 앱 기동 (Hibernate가 classroom 테이블/classroom_id 컬럼을 자동 생성)"
    docker compose up -d --build --wait app

    Write-Host "[4/6] 앱 헬스체크 대기 (Tomcat 'Server startup' 로그 확인)"
    $elapsed = 0
    while ($true) {
        $logs = docker compose logs app 2>&1 | Out-String
        if ($logs -match 'Server startup') { break }
        if ($elapsed -ge $AppReadyTimeout) {
            Write-Error "앱이 ${AppReadyTimeout}초 내에 기동되지 않았습니다. 'docker compose logs app'으로 확인하세요."
            exit 1
        }
        Start-Sleep -Seconds 2
        $elapsed += 2
    }
    Write-Host "앱 기동 완료"

    Write-Host "[5/6] 부하 테스트용 시드 데이터 적재 (classroom/student)"
    Get-Content 'src/main/java/org/example/kb7spring/student/jpa-problem.sql' -Raw |
        docker compose exec -T mysql mysql --default-character-set=utf8mb4 -uroot -p1234 kb7spring

    Write-Host "[6/6] k6 테스트 실행: $Scenario"
    docker compose --profile test run --rm k6 run `
        -e "BASE_URL=http://app:8080" `
        -e "CLASSROOM_ID=$ClassroomId" `
        -e "REQUEST_COUNT=$RequestCount" `
        -e "CAPACITY=$Capacity" `
        -e "REPEAT=$Repeat" `
        --out influxdb=http://influxdb:8086/k6 `
        "/scripts/$Scenario"
    $k6ExitCode = $LASTEXITCODE
}
finally {
    Write-Host "[cleanup] app/mysql/kafka/redis 컨테이너 종료 중... (influxdb/grafana는 k6 결과 확인을 위해 유지)"
    docker compose stop app mysql kafka redis
    Write-Host "k6 결과는 Grafana(http://localhost:3000)에서 계속 확인 가능. 완전히 정리하려면 'docker compose down' 실행"
}

exit $k6ExitCode
