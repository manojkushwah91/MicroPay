$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$propsPath = Join-Path $repoRoot ".mvn\\wrapper\\maven-wrapper.properties"
$jarPath = Join-Path $repoRoot ".mvn\\wrapper\\maven-wrapper.jar"

if (-not (Test-Path $propsPath)) {
  throw "Missing $propsPath"
}

$props = Get-Content $propsPath -Raw
$wrapperUrlLine = ($props -split "`n" | Where-Object { $_ -match '^wrapperUrl=' } | Select-Object -First 1)
if (-not $wrapperUrlLine) {
  throw "wrapperUrl not found in $propsPath"
}

$wrapperUrl = $wrapperUrlLine.Substring("wrapperUrl=".Length).Trim()
if (-not $wrapperUrl) {
  throw "wrapperUrl is empty in $propsPath"
}

New-Item -ItemType Directory -Force -Path (Split-Path -Parent $jarPath) | Out-Null

Write-Host "Downloading Maven Wrapper jar..."
Write-Host "  From: $wrapperUrl"
Write-Host "  To:   $jarPath"

Invoke-WebRequest -Uri $wrapperUrl -OutFile $jarPath

Write-Host "Done."


