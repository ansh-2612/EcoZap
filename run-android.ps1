param([string]$Serial)

$ErrorActionPreference = 'Stop'

function Find-Adb {
    $sdkFolders = @()
    if (Test-Path 'local.properties') {
        $line = Get-Content 'local.properties' | Where-Object { $_ -match '^\s*sdk\.dir\s*=' } | Select-Object -Last 1
        if ($line) {
            $sdkFolders += ($line -split '=', 2)[1].Trim().Replace('\:', ':').Replace('\\', '\').Replace('\ ', ' ')
        }
    }
    $sdkFolders += @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT)
    if ($env:LOCALAPPDATA) { $sdkFolders += Join-Path $env:LOCALAPPDATA 'Android/Sdk' }
    foreach ($folder in $sdkFolders) {
        if ($folder) {
            $candidate = Join-Path $folder 'platform-tools/adb.exe'
            if (Test-Path -LiteralPath $candidate -PathType Leaf) { return $candidate }
        }
    }
    $command = Get-Command adb.exe -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($command) { return $command.Source }
    throw 'Could not find adb. In Android Studio, open SDK Manager and install Android SDK Platform-Tools.'
}

function Invoke-Adb {
    $output = & $adb @args
    if ($LASTEXITCODE -ne 0) { throw "adb $($args -join ' ') failed (exit $LASTEXITCODE). $($output -join ' ')" }
    $output
}

Push-Location -LiteralPath $PSScriptRoot
$previousJavaHome = $env:JAVA_HOME
try {
    $adb = Find-Adb
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

    if (-not $env:JAVA_HOME -and $env:ProgramFiles) {
        $studioJava = Join-Path $env:ProgramFiles 'Android/Android Studio/jbr'
        if (Test-Path -LiteralPath "$studioJava/bin/java.exe") { $env:JAVA_HOME = $studioJava }
    }
    $revision = git rev-parse --short HEAD
    if ($LASTEXITCODE -ne 0) { throw 'Could not identify the Git checkout.' }
    Write-Host "Building EcoZap from $PSScriptRoot at commit $revision for $Serial"
    & .\gradlew.bat :app:assembleDebug
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
    $env:JAVA_HOME = $previousJavaHome
    Pop-Location
}
