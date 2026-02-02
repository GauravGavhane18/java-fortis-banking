@echo off
cls
echo.
echo ╔══════════════════════════════════════════════════════════════════════╗
echo ║                                                                      ║
echo ║              FORTIS BANKING SYSTEM - ENHANCED EDITION                ║
echo ║                                                                      ║
echo ╚══════════════════════════════════════════════════════════════════════╝
echo.
echo [STEP 1] Cleaning old build...
if exist bin rmdir /s /q bin
mkdir bin
echo [SUCCESS] Clean complete.
echo.

echo [STEP 2] Compiling Java files...
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

echo [STEP 4] Launching Fortis Banking System...
echo.
echo ════════════════════════════════════════════════════════════════════════
echo.

java -cp "bin;lib/*" com.fortis.ui.EnhancedCLI

echo.
echo ════════════════════════════════════════════════════════════════════════
echo.
echo [INFO] Application closed.
echo.
pause
