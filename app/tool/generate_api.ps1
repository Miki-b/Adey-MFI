# Regenerates packages/adey_api (the typed Dart client) from backend/openapi.json.
# NEVER edit packages/adey_api by hand: change the backend, re-export openapi.json (mvnw test), run this.
#
#   cd app; .\tool\generate_api.ps1
#
# Needs Java 17+ (for openapi-generator) and FVM. The generator jar is downloaded once into
# tool/.cache (git-ignored) and checked against Maven Central's SHA-1.
$ErrorActionPreference = 'Stop'
$GeneratorVersion = '7.26.0'

$app = Split-Path -Parent $PSScriptRoot
$spec = Join-Path $app '..\backend\openapi.json'
$out = Join-Path $app 'packages\adey_api'
$cache = Join-Path $PSScriptRoot '.cache'
$jar = Join-Path $cache "openapi-generator-cli-$GeneratorVersion.jar"

if (-not (Test-Path $jar)) {
    New-Item -ItemType Directory -Force $cache | Out-Null
    $url = "https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/$GeneratorVersion/openapi-generator-cli-$GeneratorVersion.jar"
    Write-Host "Downloading openapi-generator $GeneratorVersion"
    Invoke-WebRequest -Uri $url -OutFile $jar
    $expected = (Invoke-WebRequest -Uri "$url.sha1" -UseBasicParsing).Content.Trim()
    $actual = (Get-FileHash -Algorithm SHA1 $jar).Hash.ToLower()
    if ($actual -ne $expected) { Remove-Item $jar; throw "SHA-1 mismatch for the generator jar" }
}

# Start clean so models removed from the API disappear too.
if (Test-Path $out) { Remove-Item -Recurse -Force $out }

# Native tools write warnings to stderr; in Windows PowerShell 5.1 that would stop the script,
# so for them we rely on exit codes instead.
function Invoke-Native([scriptblock]$Command, [string]$What) {
    $ErrorActionPreference = 'Continue'
    & $Command 2>&1 | ForEach-Object { "$_" }
    if ($LASTEXITCODE -ne 0) { throw "$What failed (exit $LASTEXITCODE)" }
}

Invoke-Native { java -jar $jar generate -i $spec -g dart-dio -o $out `
    --additional-properties=pubName=adey_api,pubDescription="Generated client for the Adey MFI backend (do not edit)",serializationLibrary=json_serializable,dateLibrary=core,enumUnknownDefaultCase=true `
    --global-property=apiTests=false,modelTests=false,apiDocs=false,modelDocs=false } 'openapi-generator'

# enumUnknownDefaultCase: an enum value the backend adds later decodes as unknownDefaultOpenApi
# instead of failing the whole response.

# Post-processing (the only changes we make, all here):
#  - json_serializable now emits Dart 3.8 syntax (null-aware elements): raise the SDK floor.
#  - drop the placeholder "homepage: homepage" line.
$pubspec = Join-Path $out 'pubspec.yaml'
(Get-Content $pubspec) -replace "sdk: '>=[0-9.]+ <4.0.0'", "sdk: '^3.8.0'" -replace '^homepage: homepage$', '' |
    Set-Content -Encoding utf8 $pubspec
Remove-Item -Recurse -Force (Join-Path $out 'test'), (Join-Path $out 'doc'), (Join-Path $out '.travis.yml'), (Join-Path $out 'git_push.sh') -ErrorAction SilentlyContinue

Push-Location $out
try {
    Invoke-Native { fvm dart pub get } 'dart pub get'
    Invoke-Native { fvm dart run build_runner build --delete-conflicting-outputs } 'build_runner'
} finally {
    Pop-Location
}
Write-Host "packages/adey_api regenerated from backend/openapi.json"
