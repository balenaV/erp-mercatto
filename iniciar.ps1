$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'scripts\java.ps1')
Initialize-MercattoJava
Push-Location -LiteralPath $PSScriptRoot
try {
    & (Join-Path $PSScriptRoot 'mvnw.cmd') -ntp compile javafx:run
    if ($LASTEXITCODE -ne 0) {
        throw "A execução terminou com código $LASTEXITCODE."
    }
} finally {
    Pop-Location
}
