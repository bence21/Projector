param(
    [string]$InputDb = (Join-Path $PSScriptRoot "..\songbook-full.db"),
    [string]$OutputDb = (Join-Path $PSScriptRoot "..\app\src\androidTest\assets\songbook.db"),
    [string]$ApiBase = "http://192.168.1.134:8081"
)

$ErrorActionPreference = "Stop"
$generateScript = Join-Path $PSScriptRoot "generate-fixture-db.py"

if (-not (Test-Path $generateScript)) {
    throw "Missing generate-fixture-db.py"
}

if (-not (Test-Path $InputDb)) {
    Write-Host "No full DB at $InputDb; generating fixture directly from API $ApiBase"
    python $generateScript $ApiBase
    exit $LASTEXITCODE
}

Write-Host "Trimming fixture DB via generate-fixture-db.py using schema template $InputDb"
python $generateScript $ApiBase
exit $LASTEXITCODE
