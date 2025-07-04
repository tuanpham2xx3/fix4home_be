# Simple Payment API Test
Write-Host "=== PAYMENT API TEST ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080/api/payments"

Write-Host "Testing Payment Methods API..." -ForegroundColor Yellow
$response = Invoke-WebRequest -Uri "$baseUrl/methods" -Method GET -ContentType "application/json"
$data = $response.Content | ConvertFrom-Json

if ($data.success) {
    Write-Host "Success! Available payment methods:" -ForegroundColor Green
    $data.data | ForEach-Object {
        Write-Host "- $($_.displayName) ($($_.method))" -ForegroundColor Cyan
    }
} else {
    Write-Host "Failed: $($data.message)" -ForegroundColor Red
}

Write-Host "=== TEST COMPLETE ===" -ForegroundColor Green 