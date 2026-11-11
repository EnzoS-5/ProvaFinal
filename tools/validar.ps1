$ErrorActionPreference = 'Stop'
Push-Location "$PSScriptRoot/.."
try {
    & .\mvnw.cmd -U -B clean verify
    if ($LASTEXITCODE -ne 0) { throw 'Falha nos testes do back.' }
    Push-Location frontend
    try {
        & npm.cmd ci
        if ($LASTEXITCODE -ne 0) { throw 'Falha no npm ci.' }
        & npm.cmd test
        if ($LASTEXITCODE -ne 0) { throw 'Falha nos testes do front.' }
        & npm.cmd run build
        if ($LASTEXITCODE -ne 0) { throw 'Falha no build do front.' }
    } finally { Pop-Location }
} finally { Pop-Location }
