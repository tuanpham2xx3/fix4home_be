# Script to run Flyway Migration V026 manually
# This will create chatbot_sessions and chatbot_messages tables

$mysqlPath = "mysql"
$mysqlHost = "localhost"
$mysqlPort = "3307"
$database = "fix4home_db"
$username = "root"
$password = "pass123"
$sqlFile = "src\main\resources\db\migration\V026__Create_Chatbot_Tables.sql"

Write-Host "=== Running Migration V026: Create Chatbot Tables ===" -ForegroundColor Cyan
Write-Host "Database: $database" -ForegroundColor Yellow
Write-Host "Host: ${mysqlHost}:${mysqlPort}" -ForegroundColor Yellow
Write-Host ""

# Check if MySQL is available
try {
    $mysqlVersion = & $mysqlPath --version 2>&1
    Write-Host "MySQL found: $mysqlVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR: MySQL not found in PATH. Please install MySQL or add it to PATH." -ForegroundColor Red
    Write-Host "You can also run the SQL file manually in MySQL Workbench or phpMyAdmin" -ForegroundColor Yellow
    exit 1
}

# Check if SQL file exists
if (-not (Test-Path $sqlFile)) {
    Write-Host "ERROR: SQL file not found: $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "SQL file found: $sqlFile" -ForegroundColor Green
Write-Host ""

# Run the SQL file
Write-Host "Executing migration..." -ForegroundColor Cyan
$env:MYSQL_PWD = $password

try {
    Get-Content $sqlFile | & $mysqlPath -h $mysqlHost -P $mysqlPort -u $username $database
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "=== Migration V026 completed successfully! ===" -ForegroundColor Green
        Write-Host "Tables created:" -ForegroundColor Yellow
        Write-Host "  - chatbot_sessions" -ForegroundColor White
        Write-Host "  - chatbot_messages" -ForegroundColor White
        Write-Host ""
        Write-Host "You can now restart the application." -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "ERROR: Migration failed with exit code $LASTEXITCODE" -ForegroundColor Red
        Write-Host "Please check the error messages above." -ForegroundColor Yellow
    }
} catch {
    Write-Host ""
    Write-Host "ERROR: Failed to execute migration: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Alternative: Copy the SQL content and run it manually in MySQL Workbench" -ForegroundColor Yellow
}

# Clean up
Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue

