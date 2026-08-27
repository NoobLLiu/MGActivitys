$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$devPluginsRoot = Split-Path -Parent $projectRoot
$workspaceRoot = Split-Path -Parent $devPluginsRoot
$serverRoot = Join-Path $workspaceRoot 'StarCIty'
$devRoot = Join-Path $workspaceRoot 'dev'
$javaHome = Join-Path $serverRoot 'runtime\jdk25\jdk-25.0.3'
$buildRoot = Join-Path $projectRoot 'build'
$pluginOut = Join-Path $buildRoot 'plugin-classes'
$testOut = Join-Path $buildRoot 'test-classes'
$stubOut = Join-Path $buildRoot 'stub-classes'
$jarPath = Join-Path $buildRoot 'MGActivitys-1.0.0.jar'

$paperApi = Join-Path $serverRoot 'libraries\io\papermc\paper\paper-api\1.21.11-R0.1-SNAPSHOT\paper-api-1.21.11-R0.1-SNAPSHOT.jar'
$floodgateJar = Get-ChildItem -LiteralPath (Join-Path $serverRoot 'plugins') -Filter '*Floodgate-Spigot.jar' -File |
    Select-Object -First 1 -ExpandProperty FullName
$titleJar = Join-Path $serverRoot 'plugins\GMZCTitles-1.0.0.jar'
if (-not (Test-Path -LiteralPath $titleJar)) {
    $titleJar = Join-Path $devRoot 'local-plugins\title-system\build\GMZCTitles-1.0.0.jar'
}
$skinCacheJar = Join-Path $devRoot 'local-plugins\gmzc-skin-cache\build\GMZCSkinCache-1.0.0.jar'
$fakePlayerManagerJar = Join-Path $devPluginsRoot 'FakePlayerManager\build\FakePlayerManager-1.0.0.jar'
foreach ($dependency in @($titleJar, $skinCacheJar, $fakePlayerManagerJar)) {
    if (-not (Test-Path -LiteralPath $dependency)) {
        throw "Missing required compile dependency: $dependency"
    }
}

$baseClassPath = "$paperApi;$floodgateJar;$titleJar;$skinCacheJar;$fakePlayerManagerJar"

$libraryJars = Get-ChildItem -LiteralPath (Join-Path $serverRoot 'libraries') -Recurse -Filter '*.jar' -ErrorAction SilentlyContinue | ForEach-Object { $_.FullName }
$compileClassPath = "$baseClassPath;$stubOut;$($libraryJars -join ';')"

foreach ($path in @($pluginOut, $testOut, $stubOut)) {
    if (Test-Path -LiteralPath $path) {
        Remove-Item -LiteralPath $path -Recurse -Force
    }
    New-Item -ItemType Directory -Path $path | Out-Null
}

# compile-only stubs (e.g. integration interfaces provided by other plugins):
# compiled to stub-classes for compile classpath only; never packaged into the
# release jar (at runtime the interface is loaded from the providing plugin).
$stubSrc = Join-Path $projectRoot 'compile-only'
$stubSources = @(Get-ChildItem -LiteralPath $stubSrc -Recurse -Filter *.java -ErrorAction SilentlyContinue | ForEach-Object FullName)
if ($stubSources.Count -gt 0) {
    & (Join-Path $javaHome 'bin\javac.exe') -encoding UTF-8 -proc:none -d $stubOut $stubSources
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

$sources = Get-ChildItem -LiteralPath (Join-Path $projectRoot 'src') -Recurse -Filter *.java | ForEach-Object FullName
if ($sources.Count -gt 0) {
    & (Join-Path $javaHome 'bin\javac.exe') -encoding UTF-8 -proc:none -cp $compileClassPath -d $pluginOut $sources
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

Copy-Item -LiteralPath (Join-Path $projectRoot 'plugin.yml') -Destination $pluginOut

$testSources = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'test') -Recurse -Filter '*.java' -ErrorAction SilentlyContinue | ForEach-Object FullName)
if ($testSources.Count -gt 0) {
    & (Join-Path $javaHome 'bin\javac.exe') -encoding UTF-8 -proc:none -cp "$compileClassPath;$pluginOut" -d $testOut $testSources
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }

    $testClasses = Get-ChildItem -LiteralPath $testOut -Recurse -Filter '*Test.class' | ForEach-Object {
        ($_.FullName.Substring($testOut.Length).TrimStart('\') -replace '\\', '.' -replace '\.class$', '')
    }
    foreach ($testClass in $testClasses) {
        & (Join-Path $javaHome 'bin\java.exe') -ea -cp "$compileClassPath;$pluginOut;$testOut" $testClass
        if ($LASTEXITCODE -ne 0) {
            exit $LASTEXITCODE
        }
    }
}

if (Test-Path -LiteralPath $jarPath) {
    Remove-Item -LiteralPath $jarPath -Force
}
& (Join-Path $javaHome 'bin\jar.exe') --create --file $jarPath -C $pluginOut .
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

Write-Host "Built $jarPath"
