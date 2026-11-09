$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'scripts\java.ps1')
Initialize-MercattoJava
Push-Location -LiteralPath $PSScriptRoot
try {
    & (Join-Path $PSScriptRoot 'mvnw.cmd') -B -ntp verify
    if ($LASTEXITCODE -ne 0) {
        throw 'A verificação Maven falhou.'
    }
    $buildStamp = Get-Date -Format 'yyyyMMdd-HHmmss'
    $inputDirectory = Join-Path $PSScriptRoot ('target\app-input-' + $buildStamp)
    New-Item -ItemType Directory -Path $inputDirectory -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'target\mercatto.jar') -Destination $inputDirectory -Force
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'target\lib') -Destination $inputDirectory -Recurse -Force
    $runtimeDirectory = Join-Path $PSScriptRoot ('target\runtime-' + $buildStamp)
    & (Join-Path $env:JAVA_HOME 'bin\jlink.exe') --add-modules java.base,java.desktop,java.logging,java.scripting,jdk.unsupported,jdk.localedata --output $runtimeDirectory --strip-debug --no-header-files --no-man-pages
    if ($LASTEXITCODE -ne 0) {
        throw 'Não foi possível preparar o runtime Java.'
    }
    # Uma pasta nova por geração preserva versões já empacotadas.
    $destination = Join-Path $PSScriptRoot ('dist\' + $buildStamp)
    $arguments = @(
        '--type', 'app-image',
        '--name', 'Mercatto',
        '--app-version', '1.0.0',
        '--vendor', 'Mercatto',
        '--input', $inputDirectory,
        '--dest', $destination,
        '--main-jar', 'mercatto.jar',
        '--main-class', 'br.com.mercatto.main.Launcher',
        '--runtime-image', $runtimeDirectory,
        '--java-options', '--enable-native-access=ALL-UNNAMED'
    )
    & (Join-Path $env:JAVA_HOME 'bin\jpackage.exe') @arguments
    if ($LASTEXITCODE -ne 0) {
        throw 'Não foi possível gerar o aplicativo.'
    }
    Write-Host "Aplicativo criado em: $(Join-Path $destination 'Mercatto\Mercatto.exe')"
} finally {
    Pop-Location
}
