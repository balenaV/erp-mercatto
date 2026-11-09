# Resolve o JDK apenas para o processo atual, sem alterar as configurações do Windows.
function Initialize-MercattoJava {
    $candidates = @()
    if ($env:JAVA_HOME) {
        $candidates += $env:JAVA_HOME
    }
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $candidates += Split-Path (Split-Path $javaCommand.Source -Parent) -Parent
    }
    $jdkDirectory = Join-Path $env:USERPROFILE '.jdks'
    if (Test-Path -LiteralPath $jdkDirectory) {
        $candidates += Get-ChildItem -LiteralPath $jdkDirectory -Directory | Sort-Object Name -Descending | Select-Object -ExpandProperty FullName
    }
    foreach ($candidate in $candidates) {
        $javaPath = Join-Path $candidate 'bin\java.exe'
        $compilerPath = Join-Path $candidate 'bin\javac.exe'
        if (!(Test-Path -LiteralPath $javaPath) -or !(Test-Path -LiteralPath $compilerPath)) {
            continue
        }
        $versionOutput = & $javaPath -version 2>&1 | Out-String
        if ($versionOutput -match 'version "(?<major>\d+)' -and [int]$Matches.major -ge 21) {
            $env:JAVA_HOME = $candidate
            return
        }
    }
    throw 'Configure JAVA_HOME com um JDK 21 ou superior (ou selecione o JDK no IntelliJ).'
}
