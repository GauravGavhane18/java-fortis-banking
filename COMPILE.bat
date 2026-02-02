@echo off
chcp 65001 >nul
echo.
echo ╔══════════════════════════════════════════════════════════════════════╗
echo ║                                                                      ║
echo ║              FORTIS - COMPILE ONLY                                   ║
echo ║                                                                      ║
echo ╚══════════════════════════════════════════════════════════════════════╝
echo.

echo [STEP 1] Cleaning old build...
if exist bin rmdir /s /q bin
mkdir bin
echo [SUCCESS] Clean complete.
echo.

echo [STEP 2] Compiling Java files...
echo This may take 30-60 seconds...
echo.

REM Change to src directory and compile with relative paths
cd src
javac -encoding UTF-8 -d ..\bin -cp "..\lib\*" api\*.java core\*.java managers\*.java model\*.java persistence\*.java recovery\*.java service\*.java ui\*.java ui\components\*.java utils\*.java

if %errorlevel% neq 0 (
    cd ..
    echo.
    echo [ERROR] Compilation Failed!
    echo Please check the error messages above.
    pause
    exit /b 1
)

cd ..
echo [SUCCESS] Compilation complete.
echo.
echo You can now run the application using START.bat
echo.
pause
