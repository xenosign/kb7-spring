param(
    [ValidateSet('up', 'down')]
    [string]$Action = 'up'
)

$composeFile = Join-Path $PSScriptRoot 'docker-compose.yml'

if ($Action -eq 'down') {
    docker compose -f $composeFile down
    exit
}

docker compose -f $composeFile up -d

Write-Host ""
Write-Host "Grafana : http://localhost:3000 (admin/admin, 최초 로그인 시 비밀번호 변경)"
Write-Host "InfluxDB: http://localhost:8086 (database: k6)"
Write-Host ""
Write-Host "k6 실행 시 --out influxdb=http://localhost:8086/k6 옵션을 추가하면 Grafana의"
Write-Host "'k6' 폴더 > 'k6 Load Testing Results' 대시보드에서 결과를 확인할 수 있습니다."
