@echo off
REM Install Playwright dependencies before checking for Node.js.
REM This ensures required packages are present even if Node.js is not yet installed.

REM === Check Maven dependencies first ===
echo Checking required Maven dependencies in pom.xml

if not exist pom.xml (
  echo ERROR: pom.xml not found. Please run this script from the project root.
  exit /b 1
)

REM Check for Playwright dependency
findstr /C:"com.microsoft.playwright" pom.xml >nul 2>&1
if errorlevel 1 (
  echo ERROR: Playwright dependency not found in pom.xml.
  echo Please add the following dependency to your pom.xml:
  echo.
  echo ^<dependency^>
  echo     ^<groupId^>com.microsoft.playwright^</groupId^>
  echo     ^<artifactId^>playwright^</artifactId^>
  echo     ^<version^>1.56.0^</version^>
  echo ^</dependency^>
  echo.
  exit /b 1
)

REM Check for exec-maven-plugin
findstr /C:"exec-maven-plugin" pom.xml >nul 2>&1
if errorlevel 1 (
  echo ERROR: exec-maven-plugin not found in pom.xml.
  echo Please add the following plugin to your pom.xml ^(build/plugins section^):
  echo.
  echo ^<plugin^>
  echo     ^<groupId^>org.codehaus.mojo^</groupId^>
  echo     ^<artifactId^>exec-maven-plugin^</artifactId^>
  echo     ^<version^>3.3.0^</version^>
  echo ^</plugin^>
  echo.
  exit /b 1
)

echo Maven dependencies OK (playwright + exec-maven-plugin found in pom.xml)
echo.

REM === Check Node.js presence (required by Playwright Java at runtime) ===
where node >nul 2>&1
if errorlevel 1 (
  echo ERROR: Node.js not found on PATH.
  echo Playwright Java requires Node.js to be installed and available on PATH.
  echo Please install Node.js LTS from https://nodejs.org/ and try again.
  exit /b 1
)

echo Node.js found on PATH (required for Playwright Java runtime)
node --version
echo.

REM === Install Playwright browsers using Maven (Java CLI) ===
echo Installing Playwright browsers using Maven exec plugin
echo This will download chromium and other dependencies needed by Playwright Java
echo.

mvn org.codehaus.mojo:exec-maven-plugin:3.3.0:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"

if errorlevel 1 (
  echo.
  echo ERROR: Playwright browser installation failed.
  echo Check the Maven output above for details.
  exit /b 1
)

echo.
echo Playwright browsers installed in: %USERPROFILE%\AppData\Local\ms-playwright
echo To avoid runtime downloads by Playwright Java, set the following environment variable in your shell before running tests:
echo set PLAYWRIGHT_BROWSERS_PATH=C:\Users\<your-user>\AppData\Local\ms-playwright

echo Done.
exit /b 0
