#!/usr/bin/env pwsh

param(
    [string]$Mode = ""
)

if ([string]::IsNullOrEmpty($Mode)) {
    Write-Host "[pwsh] No Parameters,Default Skip Compilation."
}
elseif ($Mode -eq "false") {
    Write-Host "[pwsh] Skip Compilation."
}
elseif ($Mode -eq "cc") {
    Write-Host "[pwsh] Only Compilation."
    git pull
    mvn clean compile
}
else {
    Write-Host "[pwsh] Start Clean And Copy-dependencies Compilation."
    Write-Host "[pwsh] Delay 3s after remove libs"
    Write-Host "[pwsh] 3s"
    Start-Sleep -Seconds 1
    Write-Host "[pwsh] 2s"
    Start-Sleep -Seconds 1
    Write-Host "[pwsh] 1s"
    Start-Sleep -Seconds 1
    if (Test-Path "libs") {
        Remove-Item -Recurse -Force "libs"
    }
    mvn clean dependency:copy-dependencies -DoutputDirectory=libs compile
}

java -Dfile.encoding=UTF-8 -XX:+UseG1GC -classpath "./target/classes;./libs/*" io.github.gdpl2112.WzryDpApplication
