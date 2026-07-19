@echo off
setlocal

set "MODE=%~1"

if "%MODE%"=="" (
    echo [bat] No Parameters,Default Skip Compilation.
    goto run
)
if "%MODE%"=="false" (
    echo [bat] Skip Compilation.
    goto run
)
if "%MODE%"=="cc" (
    echo [bat] Only Compilation.
    git pull
    call mvn clean compile
    goto run
)

echo [bat] Start Clean And Copy-dependencies Compilation.
echo [bat] Delay 3s after remove libs
echo [bat] 3s
timeout /t 1 /nobreak >nul
echo [bat] 2s
timeout /t 1 /nobreak >nul
echo [bat] 1s
timeout /t 1 /nobreak >nul
if exist "libs" rmdir /s /q "libs"
call mvn clean dependency:copy-dependencies -DoutputDirectory=libs compile

:run
java -Dfile.encoding=UTF-8 -XX:+UseG1GC -classpath "./target/classes;./libs/*" io.github.gdpl2112.WzryDpApplication

endlocal
