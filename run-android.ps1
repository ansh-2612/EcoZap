param([string]$Serial)

$ErrorActionPreference = 'Stop'

function Test-SdkPackages([string]$folder) {
    foreach ($component in 'platforms', 'build-tools', 'cmdline-tools') {
        if (Test-Path -LiteralPath (Join-Path $folder $component) -PathType Container) { return $true }
    }
    return $false
}

function Find-AndroidTools {
    $sdkFolders = @()
    if (Test-Path 'local.properties') {
        $line = Get-Content 'local.properties' | Where-Object { $_ -match '^\s*sdk\.dir\s*=' } | Select-Object -Last 1
        if ($line) {
            $sdkFolders += ($line -split '=', 2)[1].Trim().Replace('\:', ':').Replace('\\', '\').Replace('\ ', ' ')
        }
    }
    $sdkFolders += @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT)
    if ($env:LOCALAPPDATA) { $sdkFolders += Join-Path $env:LOCALAPPDATA 'Android/Sdk' }
    $sdk = $null
    $adbPath = $null
    foreach ($folder in $sdkFolders) {
        if (-not $folder -or -not (Test-Path -LiteralPath $folder -PathType Container)) { continue }
        $candidate = Join-Path $folder 'platform-tools/adb.exe'
        $hasAdb = Test-Path -LiteralPath $candidate -PathType Leaf
        if (-not $adbPath -and $hasAdb) { $adbPath = (Get-Item -LiteralPath $candidate).FullName }
        if (-not $sdk -and ($hasAdb -or (Test-SdkPackages $folder))) {
            $sdk = (Get-Item -LiteralPath $folder).FullName
        }
    }
    if (-not $adbPath) {
        $command = Get-Command adb.exe -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
        if (-not $command) { throw 'Could not find adb. In Android Studio, open SDK Manager and install Android SDK Platform-Tools.' }
        $adbPath = $command.Source
        $toolsDir = (Get-Item -LiteralPath $adbPath).Directory
        if (-not $sdk -and $toolsDir.Name -eq 'platform-tools' -and (Test-SdkPackages $toolsDir.Parent.FullName)) {
            $sdk = $toolsDir.Parent.FullName
        }
    }
    if (-not $sdk) {
        throw 'Found adb but could not locate the Android SDK. Open Tools > SDK Manager in Android Studio, set $env:ANDROID_HOME to the Android SDK Location shown there, and run this script again.'
    }
    return @{ Sdk = $sdk; Adb = $adbPath }
}

function Find-BuildJava {
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += Join-Path $env:JAVA_HOME 'bin/java.exe' }
    if ($env:ProgramFiles) { $candidates += Join-Path $env:ProgramFiles 'Android/Android Studio/jbr/bin/java.exe' }
    $candidates += @(Get-Command java.exe -All -CommandType Application -ErrorAction SilentlyContinue | ForEach-Object { $_.Source })
    $roots = @("$env:USERPROFILE/.jdks", "$env:ProgramFiles/Microsoft", "$env:ProgramFiles/Eclipse Adoptium", "$env:ProgramFiles/Java")
    foreach ($root in $roots) {
        $candidates += @(Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue |
            ForEach-Object { Join-Path $_.FullName 'bin/java.exe' })
    }
    foreach ($java in ($candidates | Select-Object -Unique)) {
        if (-not (Test-Path -LiteralPath $java -PathType Leaf)) { continue }
        $start = New-Object System.Diagnostics.ProcessStartInfo
        $start.FileName = $java
        $start.Arguments = '-XshowSettings:properties -version'
        $start.UseShellExecute = $false
        $start.RedirectStandardError = $true
        $process = $null
        try {
            $process = [Diagnostics.Process]::Start($start)
            $settings = $process.StandardError.ReadToEnd()
            $process.WaitForExit()
            if ($process.ExitCode -ne 0) { continue }
            if ($settings -notmatch '(?m)^\s*java\.specification\.version\s*=\s*(\d+)\s*$') { continue }
            $major = [int]$Matches[1]
            if ($major -lt 17 -or $major -gt 23) { continue }
            if ($settings -notmatch '(?m)^\s*java\.home\s*=\s*(.+)$') { continue }
            $jdk = $Matches[1].Trim()
            if (Test-Path -LiteralPath "$jdk/bin/javac.exe" -PathType Leaf) { return $jdk }
        } catch [System.ComponentModel.Win32Exception] {
            continue
        } finally {
            if ($process) { $process.Dispose() }
        }
    }
    throw 'No working JDK found for EcoZap. Install Microsoft OpenJDK 17 or set JAVA_HOME to an installed JDK 17 or 21, then rerun.'
}

function Invoke-Adb {
    $output = & $adb @args
    if ($LASTEXITCODE -ne 0) { throw "adb $($args -join ' ') failed (exit $LASTEXITCODE). $($output -join ' ')" }
    $output
}

Push-Location -LiteralPath $PSScriptRoot
$previousJavaHome = $env:JAVA_HOME
$previousAndroidHome = $env:ANDROID_HOME
$previousSdkRoot = $env:ANDROID_SDK_ROOT
try {
    $androidTools = Find-AndroidTools
    $adb = $androidTools.Adb
    $env:ANDROID_HOME = $androidTools.Sdk
    if ($previousSdkRoot) { $env:ANDROID_SDK_ROOT = $androidTools.Sdk }
    Write-Host "Android SDK: $env:ANDROID_HOME"
    $devices = @(Invoke-Adb devices | ForEach-Object {
        if ($_ -match '^(\S+)\s+device\s*$') { $Matches[1] }
    })
    if ($devices.Count -eq 0) {
        throw 'No device is ready. Start your Android Studio emulator or connect and authorize your USB device, then run this again.'
    }
    if (-not $Serial -and $devices.Count -eq 1) { $Serial = $devices[0] }
    if ($Serial -notin $devices) {
        throw "Choose the device to open with -Serial DEVICE_ID. Connected devices: $($devices -join ', ')."
    }
    $androidUser = (Invoke-Adb -s $Serial shell am get-current-user).Trim()
    if ($androidUser -notmatch '^\d+$') { throw "Could not identify the active Android user on $Serial." }

    $env:JAVA_HOME = Find-BuildJava
    Write-Host "Build JDK: $env:JAVA_HOME"
    $revision = git rev-parse --short HEAD
    if ($LASTEXITCODE -ne 0) { throw 'Could not identify the Git checkout.' }
    Write-Host "Building EcoZap from $PSScriptRoot at commit $revision for $Serial"
    $buildArgs = @(':app:assembleDebug')
    if ($Serial -notlike 'emulator-*') {
        # Android's localhost reaches this computer while the USB debugging connection is active.
        try {
            Invoke-Adb -s $Serial reverse tcp:8081 tcp:8081 | Out-Null
        } catch {
            Write-Warning "USB forwarding failed. Local bookings require port 8081. $_"
        }
        $buildArgs += '-PECOZAP_DEVICE_DEFAULT_URL=http://localhost:8081'
    }
    & .\gradlew.bat @buildArgs
    if ($LASTEXITCODE -ne 0) { throw 'The Android build failed. See the Gradle error above.' }

    $apk = (Get-Item 'app/build/outputs/apk/debug/app-debug.apk').FullName
    $result = @(Invoke-Adb -s $Serial install -r --user $androidUser $apk)
    if (-not ($result -match '^Success\s*$')) { throw "Install failed. $($result -join ' ')" }

    $package = 'com.example.ecozap'
    $paths = Invoke-Adb -s $Serial shell pm path --user $androidUser $package
    $baseApk = @($paths | ForEach-Object { if ($_.Trim() -match '^package:(.+/base\.apk)$') { $Matches[1] } })
    if ($baseApk.Count -ne 1) { throw "Could not identify the installed APK on $Serial. Android returned: $paths" }
    $copy = [IO.Path]::GetTempFileName()
    try {
        Invoke-Adb -s $Serial pull $baseApk[0] $copy | Out-Null
        $hash = (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash
        if ((Get-FileHash -LiteralPath $copy -Algorithm SHA256).Hash -ne $hash) {
            throw "The installed APK on $Serial differs from the file that Gradle built."
        }
    } finally { Remove-Item -LiteralPath $copy }

    # Start at Home even if an earlier EcoZap screen was left open (NEW_TASK | CLEAR_TASK).
    $launch = Invoke-Adb -s $Serial shell am start -W --user $androidUser -f '0x10008000' -n "$package/.MainActivity"
    if (-not ($launch -match '^\s*Status:\s*ok\s*$')) { throw "EcoZap did not open on $Serial. $($launch -join ' ')" }
    Write-Host "Verified and opened EcoZap on $Serial (commit $revision)."
    Write-Host "Installed APK SHA-256: $hash"
} finally {
    $env:ANDROID_HOME = $previousAndroidHome
    $env:ANDROID_SDK_ROOT = $previousSdkRoot
    $env:JAVA_HOME = $previousJavaHome
    Pop-Location
}
