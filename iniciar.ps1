param([switch]$SinCompilar)
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (-not $SinCompilar) {
    $mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    $mavenCandidates = @(
        'C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd',
        'C:\Program Files\NetBeans-24\netbeans\java\maven\bin\mvn.cmd',
        'C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.3\plugins\maven\lib\maven3\bin\mvn.cmd'
    )
    $mavenPath = if ($mavenCommand) { $mavenCommand.Source } else {
        $mavenCandidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
    }
    if (-not $mavenPath) { throw 'Instale Maven 3.9+ y agregue mvn a PATH.' }
    $mavenCache = Join-Path $env:USERPROFILE '.m2\repository'
    & $mavenPath "-Dmaven.repo.local=$mavenCache" -B verify
    if ($LASTEXITCODE -ne 0) { throw 'La compilacion o las pruebas fallaron.' }
}
if (-not (Test-Path -LiteralPath 'target\acceso-1.0.0.jar')) { throw 'Primero compile la aplicacion.' }
if (-not $env:ADMIN_PASSWORD -and -not (Test-Path -LiteralPath 'data\acceso.mv.db')) {
    $initialSecret = Read-Host 'Contrasena inicial de admin (8-72 caracteres, mayuscula, minuscula, numero y simbolo)' -AsSecureString
    $env:ADMIN_PASSWORD = [System.Net.NetworkCredential]::new('', $initialSecret).Password
}
Write-Host 'Servidor en http://localhost:8080. Use Ctrl+C para detener.'
& java -jar target/acceso-1.0.0.jar

