@echo off
REM =============================================================================
REM CrowdCheck - Windows Launch Script
REM =============================================================================

echo ===============================================================================
echo            Starting CrowdCheck Live Crowd Monitoring System...
echo ===============================================================================

cd /d "%~dp0\.."

REM Check if maven target exists or build first
if not exist "target\crowdcheck.jar" (
    echo [*] Executable JAR not found. Building project with Maven...
    call mvn clean package -DskipTests
    if %ERRORLEVEL% neq 0 (
        echo [-] Maven build failed. Please verify Java 17+ and Maven installation.
        pause
        exit /b %ERRORLEVEL%
    )
)

echo [*] Launching application...
java -jar target\crowdcheck.jar
pause
